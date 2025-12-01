package ai.p2ach.p2achandroidvision.repos.camera.handlers


import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.getInternalCameraId
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

class InternalCameraHandler(
    private val context: Context
) : BaseCameraHandler(CameraType.INTERNAL) , KoinComponent {

   val cameraManager : CameraManager by inject()

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var characteristics: CameraCharacteristics? = null

    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private var lastFrameTimestamp: Long = System.currentTimeMillis()
    private var healthCheckerJob: Job? = null
    private var lastStatus: String = "unknown"



    private val yuv = Mat()
    private val rgb = Mat()

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera

            try {
                createCameraPreviewSession()
                lastStatus = "internal_opened"
                lastFrameTimestamp = System.currentTimeMillis()
                startHealthChecker()
            } catch (e: Throwable) {
                lastStatus = "internal_access_failed"
                stopCamera()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {

            stopCamera()
            lastStatus = "internal_disconnected"
        }

        override fun onError(camera: CameraDevice, error: Int) {

            stopCamera()
            lastStatus = "internal_error_$error"
        }
    }

    override fun startStreaming() {
        if (isStarted) return
        super.startStreaming()
        isStarted = true
        startBackgroundThread()
        openCameraInternal()
    }

    override fun stopStreaming() {
        isStarted = false
        stopCamera()

    }

    override fun pause() {
        super.pause()
        try {
            captureSession?.stopRepeating()
        } catch (_: Throwable) {
        }
    }

    override fun resume() {
        super.resume()
        val builder = captureRequestBuilder
        val session = captureSession
        if (builder == null || session == null) {
            try {
                createCameraPreviewSession()
            } catch (_: Throwable) {
            }
            return
        }
        try {
            val request = builder.build()
            session.setRepeatingRequest(request, null, backgroundHandler)
            lastStatus = "internal_opened"
        } catch (_: Throwable) {
            lastStatus = "internal_resume_failed"
        }
    }



    override fun applyAutoExposure(enabled: Boolean) {
        try {
            val builder = captureRequestBuilder ?: return
            if (enabled) {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            } else {
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            }
            captureSession?.setRepeatingRequest(builder.build(), null, backgroundHandler)
        } catch (_: Throwable) {
        }
    }

    override fun applyManualExposure(exposure: Int) {
        try {
            val builder = captureRequestBuilder ?: return
            val exposureInfo = characteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE) ?: return
            val minExposure = exposureInfo.lower
            val maxExposure = exposureInfo.upper
            val range = maxExposure - minExposure
            val exposureTime = minExposure + exposure / 100.0f * range
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTime.toLong())
            captureSession?.setRepeatingRequest(builder.build(), null, backgroundHandler)
        } catch (_: Throwable) {
        }
    }

    override fun getAutoExposureMode(): Boolean {
        return true
    }

    override fun getExposure(): Int {
        return 0
    }

    private fun openCameraInternal() {
        cameraId = cameraManager.getInternalCameraId()

        Log.d("openCameraInternal $cameraId")


        if (cameraId == null) {
            isStarted = false
            return
        }


        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId!!)
            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val availableSizes = map?.getOutputSizes(ImageFormat.YUV_420_888).orEmpty()

            val targetSize = availableSizes
                .filter { size ->
                    val ratio = size.width.toFloat() / size.height.toFloat()
                    abs(ratio - (16f / 9f)) < 0.01f
                }
                .maxByOrNull { it.width * it.height }
                ?: availableSizes.maxByOrNull { it.width * it.height }

            if (targetSize == null) {
                isStarted = false
                return
            }

            imageReader = ImageReader.newInstance(
                targetSize.width,
                targetSize.height,
                ImageFormat.YUV_420_888,
                4
            )

            var frameCounter = 0
            val frameInterval = 2

            imageReader?.setOnImageAvailableListener({ reader ->
                val image = try {
                    reader.acquireLatestImage()
                } catch (_: Throwable) {
                    null
                } ?: return@setOnImageAvailableListener

                frameCounter++
                if (frameCounter % frameInterval != 0) {
                    try { image.close() } catch (_: Throwable) {}
                    return@setOnImageAvailableListener
                }

                lastFrameTimestamp = System.currentTimeMillis()
                processImageProxy(image)
            }, backgroundHandler)

            setCameraState(CameraUiState.Connected(CameraType.INTERNAL))

            cameraManager.openCamera(cameraId!!, stateCallback, backgroundHandler)
        } catch (_: SecurityException) {

            isStarted = false
            lastStatus = "internal_permission_denied"
        } catch (_: CameraAccessException) {

            isStarted = false
            lastStatus = "internal_open_failed"
        } catch (_: Throwable) {

            isStarted = false
            lastStatus = "internal_unknown_error"
        }
    }

    private fun resolveCameraId(): String? {
//        val mdmId = mdmEntity?.cameraId?.takeIf { it.isNotBlank() }
//        if (mdmId != null) return mdmId

        return try {
            val ids = cameraManager.cameraIdList.toList()
            val front = ids.firstOrNull { id ->
                val c = cameraManager.getCameraCharacteristics(id)
                c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }
            val back = ids.firstOrNull { id ->
                val c = cameraManager.getCameraCharacteristics(id)
                c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            front ?: back ?: ids.firstOrNull()
        } catch (_: Throwable) {
            null
        }
    }

    private fun createCameraPreviewSession() {
        val device = cameraDevice ?: return
        val readerSurface = imageReader?.surface ?: return

        try {
            captureSession?.let {
                try { it.stopRepeating() } catch (_: Throwable) {}
                try { it.close() } catch (_: Throwable) {}
            }
            captureSession = null

            captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(readerSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            }

            device.createCaptureSession(
                listOf(readerSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        val req = captureRequestBuilder?.build() ?: return
                        try {
                            session.setRepeatingRequest(req, null, backgroundHandler)
                        } catch (_: Throwable) {
                            lastStatus = "internal_configure_failed"
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        lastStatus = "internal_configure_failed"
                    }
                },
                backgroundHandler
            )
        } catch (_: Throwable) {
            lastStatus = "internal_configure_exception"
        }
    }

    private fun processImageProxy(image: Image) {
        if (isPaused) {
            try { image.close() } catch (_: Throwable) {}
            return
        }

        val id = cameraId ?: "internal"
        val srcMat = toMat(image)
        try {
            processImage(srcMat, id)
        } catch (_: Throwable) {
        } finally {
            try { srcMat.release() } catch (_: Throwable) {}
            try { image.close() } catch (_: Throwable) {}
        }
    }

    private fun toMat(image: Image): Mat {
        val ySize = image.planes[0].buffer.remaining()
        val uSize = image.planes[1].buffer.remaining()
        val vSize = image.planes[2].buffer.remaining()

        val nv21 = ByteArrayPool.getByteArray(ySize + uSize + vSize)
        image.planes[0].buffer.get(nv21, 0, ySize)
        image.planes[2].buffer.get(nv21, ySize, vSize)
        image.planes[1].buffer.get(nv21, ySize + vSize, uSize)

        yuv.create(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuv.put(0, 0, nv21)
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3)
        ByteArrayPool.release(nv21)
        return rgb
    }

    private fun startBackgroundThread() {
        if (::backgroundThread.isInitialized && backgroundThread.isAlive) return
        backgroundThread = HandlerThread("InternalCameraBackground").apply { start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        healthCheckerJob?.cancel()
        healthCheckerJob = null

        if (::backgroundHandler.isInitialized) {
            backgroundHandler.removeCallbacksAndMessages(null)
        }

        if (::backgroundThread.isInitialized) {
            try {
                backgroundThread.quitSafely()
                backgroundThread.join()
            } catch (_: Throwable) {
            }
        }
    }

    @Synchronized
    private fun stopCamera() {
        try {
            imageReader?.close()
        } catch (_: Throwable) {
        } finally {
            imageReader = null
        }

        try {
            captureSession?.close()
        } catch (_: Throwable) {
        } finally {
            captureSession = null
        }

        try {
            cameraDevice?.close()
        } catch (_: Throwable) {
        } finally {
            cameraDevice = null
        }

        stopBackgroundThread()
    }

    private fun startHealthChecker() {
        healthCheckerJob?.cancel()
        healthCheckerJob = CoroutineExtension.launch{
            while (isStarted) {
                delay(10_000)
                val diff = System.currentTimeMillis() - lastFrameTimestamp
                if (diff > 30_000) {
                    if (lastStatus != "internal_timeout") {
                        lastStatus = "internal_timeout"
                    }
                } else {
                    if (lastStatus != "internal_opened") {
                        lastStatus = "internal_opened"
                    }
                }
            }
        }
    }
}