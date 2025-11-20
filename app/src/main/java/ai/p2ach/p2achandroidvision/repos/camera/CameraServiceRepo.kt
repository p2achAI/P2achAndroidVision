package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.base.repos.BaseServiceRepo
import android.content.Context
import android.os.IBinder
import android.view.SurfaceHolder

class CameraServiceRepo(
    context: Context
) : BaseServiceRepo<CameraService>(
    appContext = context.applicationContext,
    serviceClass = CameraService::class.java
) {
    private val pendingSurfaces = mutableSetOf<SurfaceHolder>()
    private val pendingActions = mutableListOf<(CameraService) -> Unit>()

    override fun getServiceFromBinder(binder: IBinder): CameraService {
        val b = binder as CameraService.LocalBinder
        return b.getService()
    }

    override fun onServiceBound(service: CameraService) {
        pendingSurfaces.forEach { holder ->
            service.attachSurface(holder)
        }
        pendingSurfaces.clear()

        pendingActions.forEach { action ->
            action(service)
        }
        pendingActions.clear()
    }

    override fun onServiceUnbound(old: CameraService?) {
    }

    fun attachPreview(holder: SurfaceHolder) {
        val service = serviceState.value
        if (service != null) {
            service.attachSurface(holder)
        } else {
            pendingSurfaces += holder
        }
    }

    fun detachPreview(holder: SurfaceHolder) {
        val service = serviceState.value
        if (service != null) {
            service.detachSurface(holder)
        }
        pendingSurfaces -= holder
    }

    fun startUsbPreview() {
        val service = serviceState.value
        if (service != null) {
            service.startUsbCamera()
        } else {
            pendingActions += { it.startUsbCamera() }
        }
    }

    fun startRtspPreview(url: String) {
        val service = serviceState.value
        if (service != null) {
            service.startRtspCamera(url)
        } else {
            pendingActions += { it.startRtspCamera(url) }
        }
    }

    fun stopPreview() {
        val service = serviceState.value
        if (service != null) {
            service.stopCamera()
        } else {
            pendingActions += { it.stopCamera() }
        }
    }
}