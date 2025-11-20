package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidlibrary.utils.Log
import android.content.Context
import android.hardware.usb.UsbDevice
import android.view.SurfaceHolder

import com.serenegiant.usb.UVCCamera
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.IFrameCallback
import android.os.Handler
import android.os.Looper
import java.nio.ByteBuffer

class UVCCameraHandler(
    private val context: Context
) : CameraHandler {

    private var usbMonitor: USBMonitor? = null
    private var uvcCamera: UVCCamera? = null
    private var previewHolder: SurfaceHolder? = null
    private var isCameraOpened = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val usbListener = object : USBMonitor.OnDeviceConnectListener {

        override fun onAttach(device: UsbDevice?) {
            Log.d("UVC onAttach device=$device")
            device?.let {
                usbMonitor?.requestPermission(it)
            }
        }

        override fun onDetach(device: UsbDevice?) {
            Log.d("UVC onDetach device=$device")
            closeCamera()
        }

        override fun onConnect(
            device: UsbDevice?,
            ctrlBlock: USBMonitor.UsbControlBlock?,
            createNew: Boolean
        ) {
            Log.d("UVC onConnect device=$device ctrlBlock=$ctrlBlock createNew=$createNew")
            if (device == null || ctrlBlock == null) return
            openCamera(ctrlBlock)
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            Log.d("UVC onDisconnect device=$device")
            closeCamera()
        }

        override fun onCancel(device: UsbDevice?) {
            Log.d("UVC onCancel device=$device")
        }
    }

    init {
        usbMonitor = USBMonitor(context, usbListener)
        usbMonitor?.register()
    }

    override fun setSurface(holder: SurfaceHolder?) {
        Log.d("UVC setSurface holder=$holder")
        previewHolder = holder

        val cam = uvcCamera ?: return
        val surface = holder?.surface ?: return

        Log.d("UVC setSurface surface.isValid=${surface.isValid}")
        cam.setPreviewDisplay(surface)

        cam.startPreview()
        Log.d("UVC startPreview from setSurface")
    }

    override fun clearSurface(holder: SurfaceHolder?) {
        Log.d("UVC clearSurface holder=$holder")
        if (previewHolder == holder) {
            previewHolder = null
        }
    }

    override fun start() {
        Log.d("UVC start() isCameraOpened=$isCameraOpened")
        if (isCameraOpened) return

        val deviceList = usbMonitor?.deviceList.orEmpty()
        Log.d("UVC deviceList size=${deviceList.size} list=$deviceList")

        val device = deviceList.firstOrNull()
        if (device != null) {
            Log.d("UVC requestPermission device=$device")
            usbMonitor?.requestPermission(device)
        } else {
            Log.e("UVC start", "No USB device connected")
        }
    }

    override fun stop() {
        Log.d("UVC stop()")
        closeCamera()
    }

    private fun openCamera(ctrlBlock: USBMonitor.UsbControlBlock) {
        if (isCameraOpened) {
            Log.d("UVC openCamera already opened")
            return
        }

        Log.d("UVC openCamera")

        val camera = UVCCamera()
        camera.open(ctrlBlock)

        val supported = camera.supportedSize
        Log.d("UVC supportedSizes=$supported")

        val targetWidth = 640
        val targetHeight = 480
        camera.setPreviewSize(
            targetWidth,
            targetHeight,
            UVCCamera.FRAME_FORMAT_YUYV
        )

        camera.setFrameCallback(object : IFrameCallback {
            override fun onFrame(frame: ByteBuffer?) {
//                Log.d("UVC onFrame size=${frame?.remaining()}")
            }
        }, UVCCamera.PIXEL_FORMAT_YUV420SP)

        val surface = previewHolder?.surface
        Log.d("UVC openCamera surface=$surface isValid=${surface?.isValid}")

        if (surface != null && surface.isValid) {
            camera.setPreviewDisplay(surface)
            camera.startPreview()
            Log.d("UVC startPreview from openCamera")
        } else {
            Log.d("UVC openCamera: surface not ready, wait and retry")

            mainHandler.postDelayed({
                val s = previewHolder?.surface
                Log.d("UVC delayed surface=$s isValid=${s?.isValid}")
                if (s != null && s.isValid) {
                    camera.setPreviewDisplay(s)
                    camera.startPreview()
                    Log.d("UVC startPreview from delayed openCamera")
                }
            }, 200)
        }

        uvcCamera = camera
        isCameraOpened = true
    }

    private fun closeCamera() {
        if (!isCameraOpened) {
            Log.d("UVC closeCamera: not opened")
            return
        }

        Log.d("UVC closeCamera")

        try {
            uvcCamera?.stopPreview()
        } catch (t: Throwable) {
            Log.e("UVC closeCamera stopPreview error=$t")
        }

        try {
            uvcCamera?.destroy()
        } catch (t: Throwable) {
            Log.e("UVC closeCamera destroy error=$t")
        }

        uvcCamera = null
        isCameraOpened = false
    }

    fun release() {
        Log.d("UVC release()")
        stop()
        usbMonitor?.unregister()
        usbMonitor?.destroy()
        usbMonitor = null
    }
}