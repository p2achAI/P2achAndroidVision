package ai.p2ach.p2achandroidvision.utils




import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.repos.mdm.CaptureReport
import ai.p2ach.p2achandroidvision.repos.mdm.MDMConverters
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.mdm.ROI

import com.hmdm.MDMService


fun String.getOrDefaultMDM(defaultValue : String): String{
    return this.ifBlank { defaultValue }
}

fun String.getOrDefaultMDM(default: Boolean): Boolean =this.toBooleanStrictOrNull() ?: default

fun String.getOrDefaultMDM(default: Int): Int =this.toIntOrNull() ?: default

fun String.getOrDefaultMDM(default: Long): Long = this.toLongOrNull() ?: default

fun String.getOrDefaultMDM(default: Float): Float =this.toFloatOrNull() ?: default

fun String.getOrDefaultMDM(default: ROI): ROI {
    return try {
        if (this.isBlank()) default
        else MDMConverters.jsonToRoi(this)
    } catch (e: Exception) {
        Log.e("ROI parse failed: ${e.message}")
        default
    }
}

fun String.getOrDefaultMDM(default: CaptureReport): CaptureReport {
    return try {
        if (this.isBlank()) default
        else MDMConverters.jsonToCaptureReport(this)
    } catch (e: Exception) {
        Log.e("CaptureReport parse failed: ${e.message}")
        default
    }
}



fun String.getOrDefaultMDM(default: List<String>): List<String> {
    return this.takeIf { it.isNotBlank() }
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: default
}


fun getNeedUpdateMDMEntity(base: MDMEntity): MDMEntity {

    return base.apply {

//        deviceName = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEVICE_NAME, "")
//            .getOrDefaultMDM(deviceName)
//
//        hwType = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.HW_TYPE, "")
//            .getOrDefaultMDM(hwType)
//
//        deviceUuid = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEVICE_UUID, "")
//            .getOrDefaultMDM(deviceUuid ?: "")




        deviceUuid = base.deviceUuid?: DeviceUtils.getOrCreateDeviceUuid()


        captureReport = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.CAPTURE_REPORT,"").getOrDefaultMDM(
            CaptureReport())


        netWorkAndApi.apply {
            rtspTimeoutMs = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.RTSP_TIMEOUT_MS, "")
                .getOrDefaultMDM(rtspTimeoutMs)


            rtspUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.RTSP_URL, "")
                .getOrDefaultMDM(rtspUrl)


            apiUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.API_URL, "")
                .getOrDefaultMDM(apiUrl)

            webviewUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.WEBVIEW_URL, "")
                .getOrDefaultMDM(webviewUrl)

            middlewareUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.MIDDLEWARE_URL, "")
                .getOrDefaultMDM(middlewareUrl)
        }

        versions.apply {

            demo_version = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEMO_VERSION, "")
                .getOrDefaultMDM(demo_version)

            broadcast_version = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.BROADCAST_VERSION, "")
                .getOrDefaultMDM(broadcast_version)

        }


        featureFlags.apply {
            appMode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.APP_MODE, "")
                .getOrDefaultMDM(appMode)


            useSmartSignService = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_SMART_SIGN_SERVICE, "")
                .getOrDefaultMDM(useSmartSignService)

            hide_buttons = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.HIDE_BUTTONS, "")
                .getOrDefaultMDM(hide_buttons)

            drawGrid = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DRAW_GRID, "")
                .getOrDefaultMDM(drawGrid)


            autoRotation = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.AUTO_ROTATION, "")
                .getOrDefaultMDM(autoRotation)

            useGzip = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_GZIP, "")
                .getOrDefaultMDM(useGzip)

            use_ota = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_OTA, "")
                .getOrDefaultMDM(use_ota)

            use_reid = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_REID, "")
                .getOrDefaultMDM(use_reid)

            use_ageGender_NpuModel = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_AGE_GENDER_NPU_MODEL, "")
                .getOrDefaultMDM(use_ageGender_NpuModel)

            useVideofile = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_VIDEO_FILE, "")
                .getOrDefaultMDM(useVideofile)


            use_pose = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_POSE, "")
                .getOrDefaultMDM(use_pose)

            use_headpose = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_HEAD_POSE, "")
                .getOrDefaultMDM(use_headpose)

            use_yolo = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_YOLO, "")
                .getOrDefaultMDM(use_yolo)

            use_par = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_PAR, "")
                .getOrDefaultMDM(use_par)

            use_deepsort = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_DEEP_SORT, "")
                .getOrDefaultMDM(use_deepsort)

            use_face = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_FACE, "")
                .getOrDefaultMDM(use_face)

            use_4split = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_4SPLIT, "")
                .getOrDefaultMDM(use_4split)

            contents_mode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.CONTENTS_MODE, "")
                .getOrDefaultMDM(contents_mode)

            flip = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.FLIP, "")
                .getOrDefaultMDM(flip)

            devMode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEV_MODE, "")
                .getOrDefaultMDM(devMode)


            use_age_comp = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_AGE_COMP, "")
                .getOrDefaultMDM(use_age_comp)

            use_draw_limit = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_DRAW_LIMIT, "")
                .getOrDefaultMDM(use_draw_limit)

            autoStartCameraActivity =
                MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.AUTO_START_CAMERA_ACTIVITY, "")
                    .getOrDefaultMDM(autoStartCameraActivity)
        }


        timingsAndParameters.apply {

            dataSendingInterval = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DATA_SENDING_INTERVAL, "")
                .getOrDefaultMDM(dataSendingInterval)

            dataCollectionInterval = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DATA_COLLECTION_INTERVAL, "")
                .getOrDefaultMDM(dataCollectionInterval)

            track_frms = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.TRACK_FRMS, "")
                .getOrDefaultMDM(track_frms)

            ageMode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.AGE_MODE, "")
                .getOrDefaultMDM(ageMode)

            genderThr = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GENDER_THR, "")
                .getOrDefaultMDM(genderThr)
        }


        roi = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROI,"").getOrDefaultMDM(ROI())


        camParam.apply {
            focal_x = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.FOCAL_X, "")
                .getOrDefaultMDM(focal_x)

            focal_y = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.FOCAL_Y, "")
                .getOrDefaultMDM(focal_y)

            rot_x = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROT_X, "")
                .getOrDefaultMDM(rot_x)

            rot_y = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROT_Y, "")
                .getOrDefaultMDM(rot_y)

            rot_z = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROT_Z, "")
                .getOrDefaultMDM(rot_z)

            did_w = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DID_W, "")
                .getOrDefaultMDM(did_w)

            did_h = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DID_H, "")
                .getOrDefaultMDM(did_h)

            did_l = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DID_L, "")
                .getOrDefaultMDM(did_l)

            did_t = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DID_T, "")
                .getOrDefaultMDM(did_t)

            scale1 = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.SCALE1, "")
                .getOrDefaultMDM(scale1)

            scale2 = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.SCALE2, "")
                .getOrDefaultMDM(scale2)
        }


        topView.apply {

            tvWidth = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.TV_WIDTH, "")
                .getOrDefaultMDM(tvWidth)

            tvHeight = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.TV_HEIGHT, "")
                .getOrDefaultMDM(tvHeight)
        }


        ga.apply {
            gaApiUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GA_API_URL, "")
                .getOrDefaultMDM(gaApiUrl)

            gaApiSecret = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GA_API_SECRET, "")
                .getOrDefaultMDM(gaApiSecret)

            gaMeasurementId =
                MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GA_MEASUREMENT_ID, "")
                    .getOrDefaultMDM(gaMeasurementId)
        }

        exposure.apply {
            p5 = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.P5,"").getOrDefaultMDM(p5)
            p95 = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.P95,"").getOrDefaultMDM(p95)
        }

        testing.apply {

            videofilepaths = MDMService.Preferences
                .get(Const.MDM.SETTING.REMOTE.KEY.VIDEO_FILE_PATHS, "")
                .getOrDefaultMDM(videofilepaths)

            videofileUris = MDMService.Preferences
                .get(Const.MDM.SETTING.REMOTE.KEY.VIDEO_FILE_URIS, "")
                .getOrDefaultMDM(videofileUris)
        }


        Log.d("caemraType ${MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.CAMERA_TYPE,"")} ")

        cameraType = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.CAMERA_TYPE,"").getOrDefaultMDM(cameraType)


        rotation = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROTATION, "")
            .getOrDefaultMDM(rotation)

    }
}

sealed class MdmUiItem {
    data class Header(val title: String) : MdmUiItem()
    data class Row(val key: String, val value: String) : MdmUiItem()
}

fun MDMEntity.toUiItems(): List<MdmUiItem> {
    val items = mutableListOf<MdmUiItem>()


    items += MdmUiItem.Header("Device")
    items += listOf(
        KeyValueItem("Device Name", deviceName),
        KeyValueItem("HW Type", hwType),
        KeyValueItem("Device UUID", deviceUuid ?: ""),
        KeyValueItem("Rotation", rotation.toString())
    ).map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Network / API")
    items += netWorkAndApi
        .toKeyValueList(MdmLabelMaps.networkAndApi)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Versions")
    items += versions
        .toKeyValueList(MdmLabelMaps.versions)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Feature Flags")
    items += featureFlags
        .toKeyValueList(MdmLabelMaps.featureFlags)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Timings / Parameters")
    items += timingsAndParameters
        .toKeyValueList(MdmLabelMaps.timingsAndParameters)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("ROI")
    items += roi
        .toKeyValueList(MdmLabelMaps.roi)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Camera Param")
    items += camParam
        .toKeyValueList(MdmLabelMaps.camParam)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Top View")
    items += topView
        .toKeyValueList(MdmLabelMaps.topView)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Google Analytics")
    items += ga
        .toKeyValueList(MdmLabelMaps.ga)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Exposure")
    items += exposure
        .toKeyValueList(MdmLabelMaps.exposure)
        .map { MdmUiItem.Row(it.key, it.value) }

    items += MdmUiItem.Header("Testing")
    items += testing
        .toKeyValueList(MdmLabelMaps.testing)
        .map { MdmUiItem.Row(it.key, it.value) }

    return items
}



