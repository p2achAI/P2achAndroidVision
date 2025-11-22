package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraType
import ai.p2ach.p2achandroidvision.repos.camera.handlers.UVCCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.views.activities.ActivityMain
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CameraService : LifecycleService() {

    val mdmRepo: MDMRepo by inject()

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
    private var currentType: CameraType? = null

    private val _frames = MutableSharedFlow<android.graphics.Bitmap>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val frames: SharedFlow<android.graphics.Bitmap> = _frames

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        collectMDM()
    }

    override fun onDestroy() {
        handlerCollectJob?.cancel()
        handler?.stopStreaming()
        handler = null
        super.onDestroy()
    }

    private fun collectMDM() {
        lifecycleScope.launch {
            mdmRepo.stream().distinctUntilChanged().collect { mdmEntity ->

                applyCameraType(mdmEntity.toCameraType())
            }
        }
    }

    private fun applyCameraType(type: CameraType) {
        if (currentType == type) return
        currentType = type

        handlerCollectJob?.cancel()
        handler?.stopStreaming()
        handler = null

        handler = when (type) {
            CameraType.UVC -> UVCCameraHandler(applicationContext)
            else -> null
        }

        val h = handler ?: return

        handlerCollectJob = lifecycleScope.launch {
            h.frames.collect { bmp ->
                _frames.emit(bmp)
            }
        }

        h.startStreaming()
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


    private fun MDMEntity.toCameraType() : CameraType{

        return when(cameraType){
            Const.CAMERA_TYPE.UVC-> CameraType.UVC
            Const.CAMERA_TYPE.RTSP-> CameraType.RTSP
            Const.CAMERA_TYPE.INTERNAL-> CameraType.INTERNAL
            else-> CameraType.UVC

        }
    }
}