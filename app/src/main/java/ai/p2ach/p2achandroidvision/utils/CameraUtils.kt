package ai.p2ach.p2achandroidvision.utils


import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager


fun CameraManager.getInternalCameraId() : String?{
    var frontCameraId: String? = null
    var backCameraId: String? = null
    this.cameraIdList.forEach { id ->
        val characteristics = this.getCameraCharacteristics(id)
        when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> {
                if (frontCameraId == null) {
                    frontCameraId = id
                }
            }
            CameraCharacteristics.LENS_FACING_BACK -> {
                if (backCameraId == null) backCameraId = id
            }
            CameraCharacteristics.LENS_FACING_EXTERNAL->{
                frontCameraId =id
            }
        }
    }
    return frontCameraId ?: backCameraId
}