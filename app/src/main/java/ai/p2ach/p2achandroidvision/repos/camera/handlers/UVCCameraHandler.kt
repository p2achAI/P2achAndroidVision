package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.utils.DeviceUtils.isCameraDevice
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.serenegiant.usb.IFrameCallback
import com.serenegiant.usb.Size
import com.serenegiant.usb.UVCCamera
import com.serenegiant.usb.USBMonitor
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class UVCCameraHandler(
    private val context: Context
) : BaseCameraHandler(CameraType.UVC) {


    private val format = UVCCamera.FRAME_FORMAT_MJPEG
    private var usbMonitor: USBMonitor? = null
    private var uvcCamera: UVCCamera? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val permissionAction = "ai.p2ach.USB_PERMISSION"

    private val usbListener = object : USBMonitor.OnDeviceConnectListener {

        override fun onAttach(device: UsbDevice?) {
            val d = device ?: return
            if (!d.isCameraDevice()) return
            requestUsbPermissionForDevice(d, "onAttach")
        }

        override fun onDetach(device: UsbDevice?) {
            val d = device ?: return
            if (!d.isCameraDevice()) return
            stopStreaming()
        }

        override fun onConnect(
            device: UsbDevice?,
            ctrlBlock: USBMonitor.UsbControlBlock?,
            createNew: Boolean
        ) {
            if (device == null || ctrlBlock == null) return
            if (!device.isCameraDevice()) return
            openCamera(ctrlBlock, device)
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            val d = device ?: return
            if (!d.isCameraDevice()) return
            stopStreaming()
        }

        override fun onCancel(device: UsbDevice?) {
        }
    }

    init {
        usbMonitor = USBMonitor(context, usbListener)
        usbMonitor?.register()
    }

    override fun startStreaming() {
        if (isStarted) return
        isStarted = true

        val devices = usbMonitor?.deviceList.orEmpty()
        val cameraDevice = devices.firstOrNull { it.isCameraDevice() }
        if (cameraDevice == null) {
            Log.e("UVC startStreaming", "No USB camera device")
            return
        }
        requestUsbPermissionForDevice(cameraDevice, "startStreaming")
    }

    override fun stopStreaming() {
        isStarted = false
        try { uvcCamera?.stopPreview() } catch (_: Throwable) {}
        try { uvcCamera?.destroy() } catch (_: Throwable) {}
        uvcCamera = null
    }

    private fun requestUsbPermissionForDevice(device: UsbDevice, where: String) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        if (usbManager.hasPermission(device)) {
            usbMonitor?.requestPermission(device)
            return
        }

        val intent = Intent(permissionAction)
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val filter = IntentFilter(permissionAction)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, received: Intent?) {
                val mgr = context.getSystemService(Context.USB_SERVICE) as UsbManager
                val granted = mgr.hasPermission(device)

                try { context.unregisterReceiver(this) } catch (_: Exception) {}

                if (granted) {
                    usbMonitor?.requestPermission(device)
                } else {
                    Log.e("UVC", "permission denied where=$where device=${device.deviceName}")
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        usbManager.requestPermission(device, permissionIntent)
    }

    private fun openCamera(ctrlBlock: USBMonitor.UsbControlBlock, device : UsbDevice?) {
        stopStreaming()

        val camera = UVCCamera()
        camera.open(ctrlBlock)

        val defaultSize = Size(0, 0, 0, 1920, 1080)
        val supportedSizes = when (format) {
            UVCCamera.FRAME_FORMAT_YUYV -> {
                UVCCamera.getSupportedSize(4, uvcCamera?.supportedSize)
            }
            UVCCamera.FRAME_FORMAT_MJPEG -> {
                UVCCamera.getSupportedSize(6, uvcCamera?.supportedSize)
            }
            else -> {
                emptyList()
            }
        }

        val fHdPixels = 1920 * 1080
        val maxSupported =
            supportedSizes.maxByOrNull { it.width * it.height } ?: defaultSize
        val maxSize = if (maxSupported.width * maxSupported.height > fHdPixels) {
            Size(0, 0, 0, 1920, 1080)
        } else {
            maxSupported
        }


        camera.setPreviewSize(maxSize.width, maxSize.height, format)

        camera.setFrameCallback(object : IFrameCallback {
            override fun onFrame(frame: ByteBuffer?) {
                if(frame == null) return
                if (!isStarted || isPaused) return

                val mat = convertToMat(frame, maxSize.width, maxSize.height)
                processImage(mat,device?.deviceId.toString())
                mat.release()
            }
        }, UVCCamera.PIXEL_FORMAT_YUV420SP)

        camera.startPreview()

        uvcCamera = camera
        isStarted = true
    }

    private fun yuyvToBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap? {
        val size = width * height * 3 / 2
        val nv21 = ByteArray(size)
        buffer.get(nv21, 0, minOf(buffer.remaining(), size))

        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, width, height), 80, out)
        val jpeg = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
    }


    private fun convertToMat(buffer: ByteBuffer, width: Int, height: Int): Mat {
        val expected = (width * height * 3) / 2 // NV12 (YUV420SP)
        val remaining = buffer.remaining()
        if (remaining < expected) {
            // Defensive: avoid reading beyond provided data
            throw IllegalArgumentException("Frame buffer too small: remaining=$remaining, expected=$expected for ${width}x${height}")
        }

        var yBytes: ByteArray? = null
        val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
        val bgrMat = Mat() // will be returned to caller; caller is responsible to release()
        try {
            // Borrow from pool and fill
            yBytes = ByteArrayPool.getByteArray(expected)
            buffer.get(yBytes, 0, expected)
            yuvMat.put(0, 0, yBytes)

            // Convert NV12 (YUV420SP) -> BGR
            Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_NV12)
            return bgrMat
        } catch (e: Throwable) {
            // If conversion failed, ensure we don't leak the Mat allocated for output
            bgrMat.release()
            throw e
        } finally {
            // Always release temporary resources
            yuvMat.release()
            if (yBytes != null) {
                ByteArrayPool.release(yBytes)
            }
        }
    }
}

