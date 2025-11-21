package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidlibrary.utils.Log
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.serenegiant.usb.IFrameCallback
import com.serenegiant.usb.UVCCamera
import com.serenegiant.usb.USBMonitor
import java.nio.ByteBuffer

class UVCCameraHandler(
    private val context: Context
) : CameraHandler {

    private var usbMonitor: USBMonitor? = null
    private var uvcCamera: UVCCamera? = null
    private var previewHolder: SurfaceHolder? = null

    private var isCameraOpened = false
    private var isUsbRegistered = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val permissionAction = "ai.p2ach.USB_PERMISSION"

    private val usbListener = object : USBMonitor.OnDeviceConnectListener {

        override fun onAttach(device: UsbDevice?) {
            Log.d("UVC onAttach device=$device")
            val d = device ?: return
            if (!d.isCameraDevice()) {
                Log.d("UVC onAttach non-camera device, ignore")
                return
            }
            requestUsbPermissionForDevice(d, "onAttach")
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
            if (!device.isCameraDevice()) {
                Log.d("UVC onConnect non-camera device, ignore")
                return
            }
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
        Log.d("UVC start() isCameraOpened=$isCameraOpened isUsbRegistered=$isUsbRegistered")
        if (isCameraOpened) return

        if (!isUsbRegistered) {
            usbMonitor?.register()
            isUsbRegistered = true
        }

        val devices = usbMonitor?.deviceList.orEmpty()
        Log.d("UVC deviceList size=${devices.size} list=$devices")

        val cameraDevice = devices.firstOrNull { it.isCameraDevice() }
        if (cameraDevice == null) {
            Log.e("UVC start", "No USB camera device connected")
            return
        }

        Log.d("UVC start request permission for camera=$cameraDevice")
        requestUsbPermissionForDevice(cameraDevice, "start")
    }

    override fun stop() {
        Log.d("UVC stop()")
        closeCamera()
    }

    private fun requestUsbPermissionForDevice(
        device: UsbDevice,
        where: String
    ) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        if (usbManager.hasPermission(device)) {
            Log.d("UVC already has permission where=$where, call usbMonitor.requestPermission")
            usbMonitor?.requestPermission(device)
            return
        }

        Log.d("UVC requestUsbPermissionForDevice where=$where device=$device")

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
                Log.d("UVC permission result where=$where granted=$granted")

                try {
                    context.unregisterReceiver(this)
                } catch (_: Exception) {
                }

                if (granted) {
                    usbMonitor?.requestPermission(device)
                } else {
                    Log.e("UVC", "USB permission denied for device=$device")
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
        if (isUsbRegistered) {
            usbMonitor?.unregister()
            isUsbRegistered = false
        }
        usbMonitor?.destroy()
        usbMonitor = null
    }
}

private fun UsbDevice.isCameraDevice(): Boolean {
    return this.deviceClass == UsbConstants.USB_CLASS_VIDEO ||
            (this.interfaceCount > 0 &&
                    this.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_VIDEO)
}