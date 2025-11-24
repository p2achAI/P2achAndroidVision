package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.base.repos.BaseServiceRepo
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraUiState
import android.content.Context
import android.graphics.Bitmap
import android.os.IBinder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class CameraServiceRepo(
    context: Context
) : BaseServiceRepo<CameraService>(
    appContext = context.applicationContext,
    serviceClass = CameraService::class.java
) {

    val frames: Flow<Bitmap> =
        serviceState.filterNotNull().flatMapLatest { it.frames }
    val uiStateFlow : Flow<CameraUiState> = serviceState.filterNotNull().flatMapLatest { it.uiState }

    override fun getServiceFromBinder(binder: IBinder): CameraService {
        val b = binder as CameraService.LocalBinder
        return b.getService()
    }

    override fun onServiceBound(service: CameraService) {
    }

    override fun onServiceUnbound(old: CameraService?) {
    }
}