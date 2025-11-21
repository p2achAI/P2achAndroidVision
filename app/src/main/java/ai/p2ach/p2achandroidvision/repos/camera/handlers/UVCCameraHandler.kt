package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.utils.DeviceUtils.isCameraDevice
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
import android.view.Surface
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

    private val mainHandler = Handler(Looper.getMainLooper())
    private val permissionAction = "ai.p2ach.USB_PERMISSION"

    private val usbListener = object : USBMonitor.OnDeviceConnectListener {

        override fun onAttach(device: UsbDevice?) {
            Log.d("UVC onAttach device=${device?.deviceName}")
            val d = device ?: return
            if (!d.isCameraDevice()) {
                Log.d("UVC onAttach non-camera device, ignore")
                return
            }
            requestUsbPermissionForDevice(d, "onAttach")
        }

        override fun onDetach(device: UsbDevice?) {
            Log.d("UVC onDetach device=${device?.deviceName}")

        }

        override fun onConnect(
            device: UsbDevice?,
            ctrlBlock: USBMonitor.UsbControlBlock?,
            createNew: Boolean
        ) {
            Log.d("UVC onConnect device=${device?.deviceName} ctrlBlock=$ctrlBlock createNew=$createNew")
            if (device == null || ctrlBlock == null) return
            if (!device.isCameraDevice()) {
                Log.d("UVC onConnect non-camera device, ignore")
                return
            }
            openCamera(ctrlBlock)
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            Log.d("UVC onDisconnect device=${device?.deviceName}")

        }

        override fun onCancel(device: UsbDevice?) {
            Log.d("UVC onCancel device=${device?.deviceName}")
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
        val cam = uvcCamera
        if (cam != null) {
            try {
                Log.d("UVC clearSurface detach surface & stopPreview")
//                cam.stopPreview()
//                cam.

                cam.setPreviewDisplay(null as Surface?)
                cam.setPreviewDisplay(null as SurfaceHolder?)
            } catch (t: Throwable) {
                Log.e("UVC clearSurface", "error while stopping preview: $t")
            }
        }

        if (previewHolder == holder) {
            previewHolder = null
        }
    }

    override fun start() {


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

        Log.d("UVC requestUsbPermissionForDevice where=$where device=${device.deviceName}")

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
                    Log.e("UVC", "USB permission denied for device=${device.deviceName}")
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

//                Log.d("onFrame")
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

    }
}

