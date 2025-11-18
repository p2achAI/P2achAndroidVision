package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.base.repos.BaseServiceRepo
import android.os.IBinder
import android.content.Context


class CameraServiceRepo(
    context: Context
) : BaseServiceRepo<CameraService>(
    appContext = context.applicationContext,
    serviceClass = CameraService::class.java
) {
    override fun getServiceFromBinder(binder: IBinder): CameraService {
        val b = binder as CameraService.LocalBinder
        return b.getService()
    }

    override fun onServiceBound(service: CameraService) {
    }

    override fun onServiceUnbound(old: CameraService?) {
    }
}