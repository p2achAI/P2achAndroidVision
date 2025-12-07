package ai.p2ach.p2achandroidvision.viewmodels




import ai.p2ach.p2achandroidvision.base.viewmodel.BaseViewModel
import ai.p2ach.p2achandroidvision.repos.camera.CameraService
import ai.p2ach.p2achandroidvision.repos.camera.CameraServiceRepo
import android.view.SurfaceHolder


class CameraViewModel(private val cameraServiceRepo: CameraServiceRepo): BaseViewModel<CameraService, CameraServiceRepo>(cameraServiceRepo) {
    val frames = cameraServiceRepo.frames
    val uiState = cameraServiceRepo.uiStateFlow
    val mdmFlow = cameraServiceRepo.mdmFlow
    val monitorFlow = cameraServiceRepo.monitorFlow
}