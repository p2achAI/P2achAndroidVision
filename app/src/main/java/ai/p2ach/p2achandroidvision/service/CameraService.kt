package ai.p2ach.p2achandroidvision.service

import ai.p2ach.p2achandroidlibrary.base.service.ServiceGetter
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder



class CameraService : Service(){

    inner class LocalBinder : Binder(), ServiceGetter<CameraService> {
        override fun getService(): CameraService = this@CameraService
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }


}