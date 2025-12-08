package ai.p2ach.p2achandroidvision.utils


import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportStatus
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraType
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraUiState
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.koin.java.KoinJavaComponent
import java.util.Calendar


object MdmLabelMaps {
    val networkAndApi = mapOf(
        "rtspUrl" to "RTSP URL",
        "rtspTimeoutMs" to "RTSP Timeout(ms)",
        "apiUrl" to "API URL",
        "webviewUrl" to "WebView URL",
        "middlewareUrl" to "Middleware URL",
        "localWebviewUrl" to "Local WebView URL"
    )

    val versions = mapOf(
        "demo_version" to "Demo Version",
        "broadcast_version" to "Broadcast Version"
    )

    val featureFlags = mapOf(
        "appMode" to "App Mode",
        "useSmartSignService" to "SmartSign",
        "hide_buttons" to "Hide Buttons",
        "drawGrid" to "Draw Grid",
        "autoRotation" to "Auto Rotation",
        "useGzip" to "Use GZip",
        "use_ota" to "Use OTA",
        "use_reid" to "Use ReID",
        "use_ageGender_NpuModel" to "Age/Gender NPU",
        "useVideofile" to "Use Video File",
        "use_pose" to "Use Pose",
        "use_headpose" to "Use HeadPose",
        "use_yolo" to "Use YOLO",
        "use_par" to "Use PAR",
        "use_deepsort" to "Use DeepSort",
        "use_face" to "Use Face",
        "use_4split" to "Use 4-Split",
        "contents_mode" to "Contents Mode",
        "flip" to "Flip",
        "devMode" to "Dev Mode",
        "use_age_comp" to "Use Age Comp",
        "use_draw_limit" to "Use Draw Limit",
        "autoStartCameraActivity" to "Auto Start Camera"
    )

    val timingsAndParameters = mapOf(
        "dataSendingInterval" to "Data Sending Interval",
        "dataCollectionInterval" to "Data Collection Interval",
        "track_frms" to "Track Frames",
        "ageMode" to "Age Mode",
        "genderThr" to "Gender Threshold"
    )

    val roi = mapOf(
        "top" to "Top",
        "left" to "Left",
        "width" to "Width",
        "height" to "Height"
    )

    val camParam = mapOf(
        "focal_x" to "Focal X",
        "focal_y" to "Focal Y",
        "rot_x" to "Rot X",
        "rot_y" to "Rot Y",
        "rot_z" to "Rot Z",
        "did_w" to "DID Width",
        "did_h" to "DID Height",
        "did_l" to "DID Left",
        "did_t" to "DID Top",
        "scale1" to "Scale1",
        "scale2" to "Scale2"
    )

    val topView = mapOf(
        "tvWidth" to "TV Width",
        "tvHeight" to "TV Height"
    )

    val ga = mapOf(
        "gaApiUrl" to "GA API URL",
        "gaApiSecret" to "GA Secret",
        "gaMeasurementId" to "GA Measurement Id"
    )

    val exposure = mapOf(
        "p5" to "Exposure P5",
        "p95" to "Exposure P95"
    )

    val testing = mapOf(
        "videofilepaths" to "Video Paths",
        "videofileUris" to "Video URIs"
    )
}



fun getCameraStatusMessage(cameraUiState: CameraUiState) : String{

    Log.d("getCameraStatusMessage $cameraUiState")

    return when(cameraUiState){

        is CameraUiState.Error ->
            when(cameraUiState.type){
            CameraType.UVC -> R.string.txt_error_connecting_uvc.getMessage()
            CameraType.RTSP ->R.string.txt_error_connecting_rtsp.getMessage()
            CameraType.INTERNAL ->R.string.txt_error_connecting_internal.getMessage()
            CameraType.NONE -> R.string.txt_error_connecting_unknown.getMessage()
        }

        is CameraUiState.Connected -> ""
        is CameraUiState.Disconnected ->{
            when(cameraUiState.type){
                CameraType.UVC -> R.string.txt_error_connecting_uvc.getMessage()
                CameraType.RTSP -> R.string.txt_error_connecting_rtsp.getMessage()
                CameraType.INTERNAL -> R.string.txt_error_connecting_internal.getMessage()
                CameraType.NONE -> R.string.txt_error_connecting_unknown.getMessage()
            }
        }
        is CameraUiState.Stoped -> ""
        is CameraUiState.Connecting -> {
            when(cameraUiState.type){
                CameraType.UVC -> R.string.txt_progress_connecting_uvc.getMessage()
                CameraType.RTSP -> R.string.txt_progress_connecting_rtsp.getMessage()
                CameraType.INTERNAL -> R.string.txt_progress_connecting_internal.getMessage()
                CameraType.NONE -> R.string.txt_progress_connecting_unknown.getMessage()

            }
        }
        is CameraUiState.Switching -> {
            when(cameraUiState.type){
                CameraType.UVC -> R.string.txt_progress_switch_uvc.getMessage()
                CameraType.RTSP -> R.string.txt_progress_connecting_rtsp.getMessage()
                CameraType.INTERNAL -> R.string.txt_progress_switch_internal.getMessage()
                CameraType.NONE -> R.string.txt_progress_switch_unknown.getMessage()
            }
        }
        is CameraUiState.Idle ->""
    }
}



fun CameraType.toDisplayName(): String =
    when (this) {
        CameraType.UVC -> R.string.txt_camera_type_uvc.getMessage()
        CameraType.INTERNAL -> R.string.txt_camera_type_internal.getMessage()
        CameraType.RTSP -> R.string.txt_camera_type_rtsp.getMessage()
        else -> R.string.txt_progress_connecting_unknown.getMessage()
    }

fun MDMEntity.toCameraType(): CameraType =
    when (cameraType.lowercase()) {
        Const.CAMERA_TYPE.UVC.lowercase() -> CameraType.UVC
        Const.CAMERA_TYPE.RTSP.lowercase() -> CameraType.RTSP
        Const.CAMERA_TYPE.INTERNAL.lowercase() -> CameraType.INTERNAL
        else -> CameraType.UVC
    }

fun MDMEntity.cameraTypeDisplayName(): String =
    toCameraType().toDisplayName()


fun List<CaptureReportStatus>.toText(selectedIndex: Int? = null): String {

    if (isEmpty()) return ""

    val sb = StringBuilder()

    this.forEachIndexed { index, it ->

        if (it.startTime.isNullOrEmpty()) return ""


        val prefix = when (it.dayOfWeek) {
            Calendar.MONDAY    -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_mon.getMessage())
            Calendar.TUESDAY   -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_tue.getMessage())
            Calendar.WEDNESDAY -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_wed.getMessage())
            Calendar.THURSDAY  -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_thu.getMessage())
            Calendar.FRIDAY    -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_fri.getMessage())
            Calendar.SATURDAY  -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_sat.getMessage())
            Calendar.SUNDAY    -> R.string.txt_capture_scheduled_weekly
                .getMessage(R.string.txt_week_sun.getMessage())
            else -> R.string.txt_capture_scheduled_everyday.getMessage()
        }

        sb.append(
            R.string.txt_capture_scheduled
                .getMessage(prefix, it.startTime)
        )
        sb.append("\n")

        sb.append(
            R.string.txt_capture_running
                .getMessage(it.currentCaptureCount, it.targetCaptureCount)
        )
        sb.append("\n")

        /*sb.append(
            R.string.txt_capture_upload_running
                .getMessage(it.uploadedCount, it.uploadTargetCount)
        )*/

        sb.append("\n\n")
    }

    return sb.toString().trim()
}




fun @receiver:StringRes Int.getMessage(): String {
    val context = KoinJavaComponent.get<Context>(Context::class.java)
    return context.getString(this)
}



fun @receiver:StringRes Int.getMessage(arg1: Any): String {
    val context = KoinJavaComponent.get<Context>(Context::class.java)
    return context.getString(this, arg1)
}

fun @receiver:StringRes Int.getMessage(vararg args: Any): String {
    val context = KoinJavaComponent.get<Context>(Context::class.java)
    return context.getString(this, *args)
}




object AppDialog {

    fun showAlert(
        context: Context,
        title: String? = null,
        message: String,
        positiveText: String = "확인",
        cancelable: Boolean = true,
        onPositive: (() -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onPositive?.invoke()
            }
            .setCancelable(cancelable)

        if (!title.isNullOrEmpty()) {
            builder.setTitle(title)
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun showConfirm(
        context: Context,
        title: String? = null,
        message: String,
        positiveText: String = "확인",
        negativeText: String = "취소",
        cancelable: Boolean = true,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onPositive?.invoke()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                dialog.dismiss()
                onNegative?.invoke()
            }
            .setCancelable(cancelable)

        if (!title.isNullOrEmpty()) {
            builder.setTitle(title)
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }
}

fun Fragment.showAlertDialog(
    title: String? = null,
    message: String,
    positiveText: String = "확인",
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null
): AlertDialog {
    return AppDialog.showAlert(
        requireContext(),
        title,
        message,
        positiveText,
        cancelable,
        onPositive
    )
}

fun Fragment.showConfirmDialog(
    title: String? = null,
    message: String,
    positiveText: String = "확인",
    negativeText: String = "취소",
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
): AlertDialog {
    return AppDialog.showConfirm(
        requireContext(),
        title,
        message,
        positiveText,
        negativeText,
        cancelable,
        onPositive,
        onNegative
    )
}


fun Fragment.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
    startActivity(intent)
}
