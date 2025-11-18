package ai.p2ach.p2achandroidvision.viewmodels

import ai.p2ach.p2achandroidlibrary.base.viewmodel.BaseViewModel
import ai.p2ach.p2achandroidvision.repos.camera.CameraService
import ai.p2ach.p2achandroidvision.repos.camera.CameraServiceRepo

class CameraViewModel(private val cameraServiceRepo: CameraServiceRepo): BaseViewModel<CameraService, CameraServiceRepo>(cameraServiceRepo) {
}