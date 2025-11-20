package ai.p2ach.p2achandroidvision.viewmodels

import ai.p2ach.p2achandroidlibrary.base.viewmodel.BaseViewModel
import ai.p2ach.p2achandroidvision.repos.camera.CameraService
import ai.p2ach.p2achandroidvision.repos.camera.CameraServiceRepo
import android.view.SurfaceHolder


class CameraViewModel(private val cameraServiceRepo: CameraServiceRepo): BaseViewModel<CameraService, CameraServiceRepo>(cameraServiceRepo) {


    fun attachPreview(holder: SurfaceHolder) {
        repo.attachPreview(holder)
    }

    fun detachPreview(holder: SurfaceHolder) {
        repo.detachPreview(holder)
    }

    fun startUsbPreview() {
        repo.startUsbPreview()
    }

    fun startRtspPreview(url: String) {
        repo.startRtspPreview(url)
    }

    fun stopPreview() {
        repo.stopPreview()
    }
}