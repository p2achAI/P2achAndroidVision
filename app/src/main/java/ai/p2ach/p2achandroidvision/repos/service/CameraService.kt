package ai.p2ach.p2achandroidvision.repos.service

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.views.fragments.P2achCameraManager
import android.app.Service
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Binder
import android.os.IBinder
import org.koin.android.ext.android.inject

class CameraService : Service() {

    val cameraManager : P2achCameraManager by inject()

    inner class LocalBinder : Binder() {
        fun getService(): CameraService = this@CameraService
    }
    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d("onCreate()")
    }
}