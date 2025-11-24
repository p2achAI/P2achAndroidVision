package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.repos.camera.handlers.FrameData.MediaImageFrame
import ai.p2ach.p2achandroidvision.repos.camera.handlers.FrameData.YuvImageFrame
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import com.google.android.renderscript.YuvFormat
import ir.am3n.rtsp.client.Rtsp
import ir.am3n.rtsp.client.data.SdpInfo
import ir.am3n.rtsp.client.data.YuvFrame
import ir.am3n.rtsp.client.interfaces.Frame
import ir.am3n.rtsp.client.interfaces.RtspFrameListener
import ir.am3n.rtsp.client.interfaces.RtspStatusListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class RTSPCameraHandler(
    private val context: Context
) : BaseCameraHandler(CameraType.RTSP) {

    private val rtsp = Rtsp()

    @Volatile private var disconnectCount = 0
    private val maxRetryConnect = 10

    private var originWidth = 0
    private var originHeight = 0

    private val startStopLock = Mutex()
    private var reconnectJob: Job? = null

    private val rtspStatusListener = object : RtspStatusListener {
        override fun onConnecting() {
            Log.d("RTSP", "onConnecting")

        }


        override fun onConnected(sdpInfo: SdpInfo) {


            disconnectCount = 0
            originWidth = sdpInfo.videoTrack?.frameWidth ?: 0
            originHeight = sdpInfo.videoTrack?.frameHeight ?: 0
            Log.d("RTSP", "onConnected ${originWidth}x${originHeight}")
        }

        override fun onDisconnected() {
            runIfNotStopped {
                Log.e("RTSP", "onDisconnected")
                disconnectCount++
                onError()
            }
        }

        override fun onDisconnecting() {
            Log.d("RTSP", "onDisconnecting")
        }

        override fun onFirstFrameRendered() {}

        override fun onUnauthorized() {
            runIfNotStopped {
                Log.e("RTSP", "onUnauthorized")
                disconnectCount++
                onError()
            }
        }

        override fun onFailed(message: String?) {
            runIfNotStopped {
                Log.e("RTSP", "onFailed $message")
                disconnectCount++
                onError()
            }
        }

        private fun onError() {
            val url = mdmEntity?.netWorkAndApi?.rtspUrl.orEmpty()
            val timeoutMs = mdmEntity?.netWorkAndApi?.rtspTimeoutMs ?: 5_000
            if (url.isEmpty()) return

            rtsp.init(url, timeout = timeoutMs)

            reconnectJob?.cancel()

            val timeoutDelay = when (disconnectCount) {
                in 0..2 -> 500L
                in 3..6 -> 1_000L
                in 7..maxRetryConnect -> 3_000L
                else -> 5_000L
            }

            reconnectJob = CoroutineScope(Dispatchers.IO).launch {
                delay(timeoutDelay)
                if (!rtsp.isStarted() && isStarted) {
                    Log.d("RTSP", "reconnect start")
                    rtsp.start(playVideo = true, playAudio = false)
                }
            }
        }
    }

    private val rtspFrameListener = object : RtspFrameListener {
        private val maxQueueSize = 10
        private val isProcessingFrame = AtomicBoolean(false)
        private val frameQueue = ArrayBlockingQueue<FrameData>(maxQueueSize)

        init {
            Thread(FrameProcessor(), "RTSP-FrameProcessor").start()
        }

        override fun onVideoNalUnitReceived(frame: Frame?) {
            disconnectCount = 0
        }

        override fun onAudioSampleReceived(frame: Frame?) {}

        override fun onVideoFrameReceived(
            width: Int,
            height: Int,
            mediaImage: Image?,
            yuv: YuvFrame?,
            bitmap: Bitmap?
        ) {
            if (!isStarted || isPaused) {
                mediaImage?.close()
                return
            }


            if (originWidth == 0 || originHeight == 0) {
                originWidth = width
                originHeight = height
            }


//            Log.d("RTSP yuv format=${yuv?.format} size=${yuv?.data?.size} w=$width h=$height")

            when {
                mediaImage != null -> {
                    val fd = MediaImageFrame(mediaImage)
                    if (!frameQueue.offer(fd)) {
                        frameQueue.poll()?.safeClose()
                        frameQueue.offer(fd)
                    }
                }

                yuv != null -> {
                    val fd = YuvImageFrame(yuv, width, height)
                    if (!frameQueue.offer(fd)) {
                        frameQueue.poll()
                        frameQueue.offer(fd)
                    }
                }

                bitmap != null -> {
                    emitFrame(bitmap)
                }
            }
        }

        inner class FrameProcessor : Runnable {
            override fun run() {
                while (true) {
                    val frame = try {
                        frameQueue.take()
                    } catch (e: InterruptedException) {
                        continue
                    }

                    if (isProcessingFrame.get()) {
                        frame.safeClose()
                        continue
                    }

                    isProcessingFrame.set(true)
                    try {
                        processFrame(frame)
                    } catch (_: Throwable) {
                    } finally {
                        isProcessingFrame.set(false)
                    }
                }
            }

            private fun processFrame(frame: FrameData) {
                when (frame) {
                    is MediaImageFrame -> {
                        val src = mediaImageToMat(frame.mediaImage)
                        try {
                            if (src != null) {
                                processImage(src, mdmEntity?.netWorkAndApi?.rtspUrl)
                            }
                        } finally {
                            src?.release()
                            frame.mediaImage.close()
                        }
                    }

                    is YuvImageFrame -> {
                        val src = yuvToMat(frame.yuvFrame, frame.width, frame.height, mdmEntity?.rotation ?: 0)
                        try {
                            processImage(src, mdmEntity?.netWorkAndApi?.rtspUrl)
                        } finally {
                            src.release()
                        }
                    }
                }
            }
        }
    }

    init {
        initPlayer()
    }

    private fun initPlayer() {
        CoroutineScope(Dispatchers.IO).launch {
            val url = mdmEntity?.netWorkAndApi?.rtspUrl.orEmpty()
            if (url.isEmpty()) {
                Log.e("RTSP", "rtspUrl empty")
                return@launch
            }

            val isOnline = withContext(Dispatchers.IO) { Rtsp.isOnline(url) }
            if (!isOnline) {
                Log.e("RTSP", "rtspUrl offline $url")
                return@launch
            }

            startStreaming()
        }
    }

    override fun startStreaming() {
        CoroutineScope(Dispatchers.IO).launch {
            startStopLock.withLock {
                if (rtsp.isStarted()) return@withLock

                val url = mdmEntity?.netWorkAndApi?.rtspUrl.orEmpty()
                if (url.isEmpty()) return@withLock

                isStarted = true

                val timeoutMs = mdmEntity?.netWorkAndApi?.rtspTimeoutMs ?: 5_000
                rtsp.init(url, timeout = timeoutMs)

                rtsp.setStatusListener(rtspStatusListener)
                rtsp.setFrameListener(rtspFrameListener)
                rtsp.setRequestYuv(true)
                rtsp.start(playVideo = true, playAudio = false)
            }
        }
    }

    override fun stopStreaming() {
        runBlocking {
            startStopLock.withLock {
                isStarted = false
                reconnectJob?.cancel()

                rtsp.setStatusListener(null)
                rtsp.setFrameListener(null)

                if (rtsp.isStarted()) {
                    rtsp.stop()
                }
            }
        }
    }

    private fun runIfNotStopped(block: () -> Unit) {
        if (isStarted) block()
    }

    private fun yuvToMat(yuvFrame: YuvFrame, width: Int, height: Int, rotation: Int): Mat {
        val data = yuvFrame.data
        val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
        yuvMat.put(0, 0, data)

        val bgrMat = Mat()
        try {
            val cvtCode = when (yuvFrame.format) {
                YuvFormat.NV21 -> Imgproc.COLOR_YUV2BGR_NV21
                YuvFormat.NV12 -> Imgproc.COLOR_YUV2BGR_NV12
                YuvFormat.YV12 -> Imgproc.COLOR_YUV2BGR_I420
//                YuvFormat.I420 -> Imgproc.COLOR_YUV2BGR_I420
                else -> Imgproc.COLOR_YUV2BGR_I420
            }

            Imgproc.cvtColor(yuvMat, bgrMat, cvtCode)

            val cropped = if (originWidth > 0 && originHeight > 0 &&
                originWidth <= bgrMat.cols() && originHeight <= bgrMat.rows()
            ) {
                bgrMat.submat(0, originHeight, 0, originWidth)
            } else {
                bgrMat
            }

            if (rotation > 0) {
                val rotated = Mat()
                Core.rotate(cropped, rotated, rotation - 1)
                if (cropped !== bgrMat) cropped.release()
                bgrMat.release()
                return rotated
            }

            if (cropped !== bgrMat) {
                val out = cropped.clone()
                cropped.release()
                bgrMat.release()
                return out
            }

            return bgrMat
        } finally {
            yuvMat.release()
        }
    }

    private fun mediaImageToMat(image: Image): Mat? {
        return try {
            if (image.format != ImageFormat.YUV_420_888) return null

            val crop = image.cropRect
            val width = crop.width()
            val height = crop.height()

            val yuvBytes = ByteArray(width * height * 3 / 2)
            val planes = image.planes
            var offset = 0

            for (i in planes.indices) {
                val buffer = planes[i].buffer
                val rowStride = planes[i].rowStride
                val pixelStride = planes[i].pixelStride
                val planeW = if (i == 0) width else width / 2
                val planeH = if (i == 0) height else height / 2

                val rowData = ByteArray(rowStride)
                for (row in 0 until planeH) {
                    if (pixelStride == 1) {
                        buffer.get(yuvBytes, offset, planeW)
                        offset += planeW
                        if (row < planeH - 1) {
                            buffer.position(buffer.position() + rowStride - planeW)
                        }
                    } else {
                        buffer.get(rowData, 0, minOf(rowStride, buffer.remaining()))
                        for (col in 0 until planeW) {
                            yuvBytes[offset++] = rowData[col * pixelStride]
                        }
                    }
                }
            }

            val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
            yuvMat.put(0, 0, yuvBytes)

            val bgrMat = Mat()
            Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420)
            yuvMat.release()
            bgrMat
        } catch (_: Throwable) {
            null
        }
    }

    private fun yv12ToMat(data: ByteArray, width: Int, height: Int): Mat {
        val ySize = width * height
        val uvSize = ySize / 4

        val i420 = ByteArray(ySize + uvSize * 2)

        // y 그대로
        System.arraycopy(data, 0, i420, 0, ySize)

        // YV12 = Y + V + U
        // I420 = Y + U + V
        val vStart = ySize
        val uStart = ySize + uvSize

        System.arraycopy(data, vStart, i420, ySize, uvSize)         // V → U
        System.arraycopy(data, uStart, i420, ySize + uvSize, uvSize) // U → V

        val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
        yuvMat.put(0, 0, i420)

        val bgr = Mat()
        Imgproc.cvtColor(yuvMat, bgr, Imgproc.COLOR_YUV2BGR_I420)
        yuvMat.release()
        return bgr
    }

}

sealed class FrameData {
    class YuvImageFrame(val yuvFrame: YuvFrame, val width: Int, val height: Int) : FrameData()
    class MediaImageFrame(val mediaImage: Image) : FrameData()
}

fun FrameData.safeClose() {
    if (this is FrameData.MediaImageFrame) {
        try {
            mediaImage.close()
        } catch (_: Throwable) {
        }
    }
}