package ai.p2ach.p2achandroidvision.views.fragments


import ai.p2ach.p2achandroidlibrary.base.fragments.isCameraDevice
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import android.hardware.camera2.CameraCharacteristics.LENS_FACING
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata.LENS_FACING_EXTERNAL
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class P2achCameraManager(private val cameraManager: CameraManager ,
                         private val usbManager: UsbManager,
                         private val mdmRepo: MDMRepo
    ) {

    enum class CameraType {
        INTERNAL, USB_UVC, RTSP, VIDEO_FILE
    }


    fun detectCameraType(): CameraType {

        var externalCamCnt = 0
        var internalCamCnt = 0

        for(id in cameraManager.cameraIdList) {
            val chars = cameraManager.getCameraCharacteristics(id)
            val facing = chars.get(LENS_FACING)
            val isExternal = facing == LENS_FACING_EXTERNAL
            if (isExternal) externalCamCnt++ else internalCamCnt++
        }

        val hasUvcCamera = usbManager.deviceList.values.any { device ->
            device.isCameraDevice()
        }.also { found ->
            if (found) {
                usbManager.deviceList.values.firstOrNull { it.isCameraDevice() }?.let { d ->
                    MonitoringData.CAM_VID = d.vendorId
                    MonitoringData.CAM_PID = d.productId
                }
            }
        }

        return when {
            hasUvcCamera -> CameraType.USB_UVC
            internalCamCnt > 0 -> CameraType.INTERNAL
            else -> CameraType.RTSP
        }

    }





    object MonitoringData {
        var CAM_ID : String = "unknown"
        var CAM_VID : Int = -1
        var CAM_PID : Int = -1
        var CAM_HEALTH : Boolean = false
        var CAM_STATUS : String = "unknown"
        var CAM_STATUS_LOG : String = ""
        var CAM_ROTATION : Int = -1
        var CAM_FLIP : Boolean = false
        var CAM_RESOLUTION : String = "unknown"
        var LAST_FRAME_TS : Long = 0L
        var PROCESSED_FRAME_COUNT : Long = 0L
    }


}