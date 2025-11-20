package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.views.activities.ActivityMain
import ai.p2ach.p2achandroidvision.views.fragments.P2achCameraManager
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CameraService : LifecycleService() {

    val cameraManager: P2achCameraManager by inject()
    val mdmRepo: MDMRepo by inject()

    inner class LocalBinder : Binder() {
        fun getService(): CameraService = this@CameraService
    }

    private val binder = LocalBinder()



    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return  binder
    }




    //    override fun onBind(intent: Intent): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        Log.d("onCreate()")
        startForegroundWithNotification()
        collectMDM()

    }

    fun collectMDM(){
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
}