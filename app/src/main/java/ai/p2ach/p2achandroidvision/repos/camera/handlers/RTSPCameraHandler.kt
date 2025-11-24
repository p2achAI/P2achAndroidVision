package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidlibrary.utils.Log

import ai.p2ach.p2achandroidvision.repos.camera.handlers.FrameData.YuvImageFrame
import ai.p2ach.p2achandroidvision.repos.camera.handlers.FrameData.MediaImageFrame


import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
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

        }

        override fun onConnected(sdpInfo: SdpInfo) {
            disconnectCount = 0


            originWidth = sdpInfo.videoTrack?.frameWidth ?: 0
            originHeight = sdpInfo.videoTrack?.frameHeight ?: 0


        }

        override fun onDisconnected() {
            runIfNotStopped {

                disconnectCount++
                onError()

            }
        }

        override fun onDisconnecting() {

        }

        override fun onFirstFrameRendered() {}

        override fun onUnauthorized() {
            runIfNotStopped {

                disconnectCount++
                onError()
            }
        }

        override fun onFailed(message: String?) {
            runIfNotStopped {

                disconnectCount++
                onError()
            }
        }

        private fun onError() {


            rtsp.init(mdmEntity!!.netWorkAndApi.rtspUrl, timeout = mdmEntity?.netWorkAndApi?.rtspTimeoutMs?:0)

            reconnectJob?.cancel()

            val timeoutDelay = when (disconnectCount) {
                in 0..2 -> 500L
                in 3..6 -> 1_000L
                in 7..maxRetryConnect -> 3_000L
                else -> 5_000L
            }

            reconnectJob = CoroutineScope(Dispatchers.IO).launch {
                delay(timeoutDelay)
                if(!rtsp.isStarted()) {
                    rtsp.start()
                }
            }


        }
    }

    private val rtspFrameListener = object : RtspFrameListener {
        private val maxQueueSize = 10
        private val isProcessingFrame = AtomicBoolean(false)
        private val frameQueue = ArrayBlockingQueue<FrameData>(maxQueueSize)

        init {
            Thread(FrameProcessor()).start()
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

            when {
                mediaImage != null -> {
                    val frameData = MediaImageFrame(mediaImage)
                    if (!frameQueue.offer(frameData)) {
                        frameQueue.poll()?.safeClose()
                        frameQueue.offer(frameData)
                    }
                }
                yuv != null -> {
                    val frameData = YuvImageFrame(yuv, width, height)
                    if (!frameQueue.offer(frameData)) {
                        frameQueue.poll()
                        frameQueue.offer(frameData)
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
                    try {
                        val frame = frameQueue.take()

                        if (isProcessingFrame.get()) {
                            frame.safeClose()
                            continue
                        }

                        isProcessingFrame.set(true)
                        processFrame(frame)
                        isProcessingFrame.set(false)
                    } catch (_: InterruptedException) {
                    }
                }
            }

            private fun processFrame(frame: FrameData) {
                when (frame) {
                    is MediaImageFrame -> {
                        val srcMat = mediaImageToMat(frame.mediaImage)
                        try {
                            if (srcMat != null) {
                                processImage(srcMat, mdmEntity!!.netWorkAndApi.rtspUrl)
                            }
                        } finally {
                            srcMat?.release()
                            frame.mediaImage.close()
                        }
                    }
                    is YuvImageFrame -> {
                        val srcMat = toMat(frame.yuvFrame.data, frame.width, frame.height, mdmEntity?.rotation?:0)
                        try {

                                processImage(srcMat, mdmEntity!!.netWorkAndApi.rtspUrl)

                        } finally {
                            srcMat.release()
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

            if (mdmEntity?.netWorkAndApi?.rtspUrl?.isEmpty() == true) {
                Log.e("please check rtsp url.")
                return@launch
            }
            if(mdmEntity == null){
                Log.e("mdmEntity == null")
                return@launch
            }

            val isOnline = withContext(Dispatchers.IO) { Rtsp.isOnline(mdmEntity!!.netWorkAndApi.rtspUrl) }
            startStreaming()
        }
    }


    override fun startStreaming() {
        CoroutineScope(Dispatchers.IO).launch {
            startStopLock.withLock {
                if (rtsp.isStarted()) return@withLock


                isStarted = true

                val timeoutMs = mdmEntity?.netWorkAndApi?.rtspTimeoutMs?:5_000
                rtsp.init(mdmEntity!!.netWorkAndApi.rtspUrl, timeout = timeoutMs)

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
       block()

    }

    private fun toMat(byteArray: ByteArray, width: Int, height: Int, rotation: Int): Mat {
        val yuv = Mat(height + height / 2, width, CvType.CV_8UC1)
        yuv.put(0, 0, byteArray)
        try {
            val mat = Mat()
            Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2BGR_YV12, 3)

            if (originWidth > 0 && originHeight > 0) {
                val validMat = mat.submat(0, originHeight, 0, originWidth)
                if (rotation > 0) {
                    val rotateImg = Mat()
                    Core.rotate(validMat, rotateImg, rotation - 1)
                    return rotateImg
                } else {
                    return validMat
                }
            }
            return mat
        } finally {
            yuv.release()
        }
    }

    private fun mediaImageToMat(image: Image): Mat? {
        return try {
            if (image.format != ImageFormat.YUV_420_888) {
                return null
            }

            val cropRect = image.cropRect
            val width = cropRect.width()
            val height = cropRect.height()

            val yuvBytes = ByteArray(width * height * 3 / 2)
            val planes = image.planes
            var offset = 0

            for (i in planes.indices) {
                val buffer = planes[i].buffer
                val rowStride = planes[i].rowStride
                val pixelStride = planes[i].pixelStride
                val planeWidth = if (i == 0) width else width / 2
                val planeHeight = if (i == 0) height else height / 2

                val rowData = ByteArray(rowStride)
                for (row in 0 until planeHeight) {
                    if (pixelStride == 1) {
                        buffer.get(yuvBytes, offset, planeWidth)
                        offset += planeWidth
                        if (row < planeHeight - 1) {
                            buffer.position(buffer.position() + rowStride - planeWidth)
                        }
                    } else {
                        buffer.get(rowData, 0, minOf(rowStride, buffer.remaining()))
                        for (col in 0 until planeWidth) {
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