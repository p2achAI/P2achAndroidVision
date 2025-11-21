package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.UVCCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.utils.DeviceUtils.isCameraDevice
import ai.p2ach.p2achandroidvision.views.activities.ActivityMain
import ai.p2ach.p2achandroidvision.views.fragments.P2achCameraManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.IBinder
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CameraService : LifecycleService() {

    val cameraManager: P2achCameraManager by inject()
    val mdmRepo: MDMRepo by inject()

    private var handler: CameraHandler? = null
    private var currentSurface: SurfaceHolder? = null

    private var usbReceiver: BroadcastReceiver? = null

    inner class LocalBinder : Binder() {
        fun getService(): CameraService = this@CameraService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("CameraService onCreate()")
        startForegroundWithNotification()
        collectMDM()
        registerUsbReceiver()
    }

    override fun onDestroy() {
        Log.d("CameraService onDestroy()")
        unregisterUsbReceiver()
        stopCameraInternal()
        super.onDestroy()
    }

    private fun collectMDM() {
        lifecycleScope.launch {
            mdmRepo.stream().distinctUntilChanged().collect { mdmEntity ->
                Log.d("CameraService mdmEntity=$mdmEntity")
            }
        }
    }

    private fun startForegroundWithNotification() {
        createNotificationChannel()

        val notificationIntent = Intent(applicationContext, ActivityMain::class.java).apply {
            putExtra("from_notification", true)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(
            applicationContext,
            Const.Service.CHANNEL_ID
        )
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.txt_notification_message))
            .setSmallIcon(R.drawable.ic_app_icon_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Const.Service.CHANNEL_ID,
            Const.Service.CHNNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun registerUsbReceiver() {
        if (usbReceiver != null) return

        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val device =
                    intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return

                if (!device.isCameraDevice()) return

                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> onUsbCameraAttached(device)
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> onUsbCameraDetached(device)
                }
            }
        }

        ContextCompat.registerReceiver(
            this,
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterUsbReceiver() {
        usbReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {
            }
        }
        usbReceiver = null
    }

    private fun onUsbCameraAttached(device: UsbDevice) {
        Log.d("CameraService onUsbCameraAttached $device")
        startUsbCamera()
    }

    private fun onUsbCameraDetached(device: UsbDevice) {
        Log.d("CameraService onUsbCameraDetached $device")
        stopCamera()
    }

    fun attachSurface(holder: SurfaceHolder) {
        Log.d("CameraService attachSurface")
        currentSurface = holder
        handler?.setSurface(holder)
    }

    fun detachSurface(holder: SurfaceHolder) {
        Log.d("CameraService detachSurface")
        if (currentSurface == holder) {
            currentSurface = null
        }
        handler?.clearSurface(holder)
    }

    fun startUsbCamera() {
        Log.d("CameraService startUsbCamera")
        if (handler is UVCCameraHandler) {
            handler?.start()
            return
        }
        stopCameraInternal()
        val uvc = UVCCameraHandler(applicationContext)
        handler = uvc
        currentSurface?.let { uvc.setSurface(it) }
        uvc.start()
    }

    fun startRtspCamera(url: String) {
        Log.d("CameraService startRtspCamera url=$url")
    }

    fun stopCamera() {
        Log.d("CameraService stopCamera")
        stopCameraInternal()
    }

    private fun stopCameraInternal() {
        handler?.stop()
        if (handler is UVCCameraHandler) {
            (handler as UVCCameraHandler).release()
        }
        handler = null
    }
}