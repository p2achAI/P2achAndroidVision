package ai.p2ach.p2achandroidvision.repos.camera


import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraType
import ai.p2ach.p2achandroidvision.repos.camera.handlers.UVCCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.views.activities.ActivityMain

import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraUiState
import ai.p2ach.p2achandroidvision.repos.camera.handlers.InternalCameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.RTSPCameraHandler
import ai.p2ach.p2achandroidvision.repos.monitoring.MonitoringRepo
import ai.p2ach.p2achandroidvision.repos.receivers.UVCCameraReceiver
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CameraService : LifecycleService() {

    private val uvcCameraHandler: UVCCameraHandler by inject()
    private val rtspCameraHandler: RTSPCameraHandler by inject()
    private val internalCameraHandler: InternalCameraHandler by inject()
    private val captureRepo: CaptureReportRepo by inject()
    private val monitoringRepo: MonitoringRepo by inject()

    val mdmRepo: MDMRepo by inject()


    private lateinit var uvcCameraReceiver: UVCCameraReceiver

    inner class LocalBinder : Binder() {
        fun getService(): CameraService = this@CameraService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    private var handler: BaseCameraHandler? = null
    private var handlerCollectJob: Job? = null
    private var handlerStateJob: Job? = null
    private var currentType: CameraType? = null

    private val _frames = MutableSharedFlow<android.graphics.Bitmap>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val frames: SharedFlow<android.graphics.Bitmap> = _frames

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()




    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()

        uvcCameraReceiver = UVCCameraReceiver(
            context = this,
            onAttached = ::onUsbCameraAttached,
            onDetached = ::onUsbCameraDetached
        )
        uvcCameraReceiver.register()

        collectMDM()



    }





    override fun onDestroy() {
        uvcCameraReceiver.unregister()

        handlerCollectJob?.cancel()
        handler?.stopStreaming()
        handler = null

        super.onDestroy()
    }

    private fun collectMDM() {
        lifecycleScope.launch {
            mdmRepo.stream().distinctUntilChanged().filterNotNull().collect { mdmEntity ->
                Log.d("MDM collect $mdmEntity")
                applyCameraType(mdmEntity.toCameraType(),mdmEntity)
                captureRepo.bindHandler(handler,mdmEntity)
                monitoringRepo.bindHandler(handler,mdmEntity)
            }
        }
    }



    private fun applyCameraType(type: CameraType,mdmEntity: MDMEntity?)  {
        Log.d("CameraService applyCameraType currType=$currentType newType=$type")
        if (currentType == type) return
        currentType = type

        _uiState.value = CameraUiState.Switching(type)

        handlerCollectJob?.cancel()
        handler?.stopStreaming()
        handler = null

        handler = when (type) {
            CameraType.UVC -> uvcCameraHandler.withMDM(mdmEntity)
            CameraType.RTSP -> rtspCameraHandler.withMDM(mdmEntity)
            CameraType.INTERNAL -> internalCameraHandler.withMDM(mdmEntity)
            else -> null
        }

        val h = handler ?: return

        handlerCollectJob = lifecycleScope.launch {
            h.frames.collect { bmp ->
                _frames.emit(bmp)
            }
        }

        handlerStateJob = lifecycleScope.launch {
            h.uiState.collect { state ->
                _uiState.value = state
            }
        }


        h.startStreaming()



    }

    private fun onUsbCameraAttached(device: UsbDevice) {
        Log.d("CameraService onUsbCameraAttached ${device.deviceName} currType=$currentType")

        if (currentType == CameraType.UVC) {
            uvcCameraHandler.startStreaming()
        }

        if(currentType == CameraType.INTERNAL){
            internalCameraHandler.startStreaming()
        }
    }

    private fun onUsbCameraDetached(device: UsbDevice) {
        Log.d("CameraService onUsbCameraDetached ${device.deviceName} currType=$currentType")

        if (currentType == CameraType.UVC) {
            uvcCameraHandler.stopStreaming()
            uvcCameraHandler.onDisconnected()
        }

        if(currentType == CameraType.INTERNAL){
            internalCameraHandler.stopStreaming()
            internalCameraHandler.onDisconnected()
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

    private fun MDMEntity.toCameraType(): CameraType {


        Log.d("cameraType ${this.cameraType}")
        return when (cameraType.lowercase()) {
            Const.CAMERA_TYPE.UVC.lowercase() -> CameraType.UVC
            Const.CAMERA_TYPE.RTSP.lowercase() -> CameraType.RTSP
            Const.CAMERA_TYPE.INTERNAL.lowercase() -> CameraType.INTERNAL
            else -> CameraType.UVC
        }
    }
}