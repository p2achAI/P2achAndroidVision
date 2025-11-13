package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import com.hmdm.MDMService


fun String.getOrDefaultMDM(defaultValue : String): String{
    return this.ifBlank { defaultValue }
}

fun String.getOrDefaultMDM(default: Boolean): Boolean =this.toBooleanStrictOrNull() ?: default

fun String.getOrDefaultMDM(default: Int): Int =this.toIntOrNull() ?: default

fun String.getOrDefaultMDM(default: Long): Long = this.toLongOrNull() ?: default

fun String.getOrDefaultMDM(default: Float): Float =this.toFloatOrNull() ?: default


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

        rtspTimeoutMs = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.RTSP_TIMEOUT_MS, "")
            .getOrDefaultMDM(rtspTimeoutMs)

        Log.d("rtspTimeoutMs $rtspTimeoutMs")


        rtspUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.RTSP_URL, "")
            .getOrDefaultMDM(rtspUrl)

        apiUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.API_URL, "")
            .getOrDefaultMDM(apiUrl)

        webviewUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.WEBVIEW_URL, "")
            .getOrDefaultMDM(webviewUrl)

        middlewareUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.MIDDLEWARE_URL, "")
            .getOrDefaultMDM(middlewareUrl)

        appMode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.APP_MODE, "")
            .getOrDefaultMDM(appMode)

        demo_version = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEMO_VERSION, "")
            .getOrDefaultMDM(demo_version)

        broadcast_version = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.BROADCAST_VERSION, "")
            .getOrDefaultMDM(broadcast_version)

        useSmartSignService = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_SMART_SIGN_SERVICE, "")
            .getOrDefaultMDM(useSmartSignService)

        hide_buttons = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.HIDE_BUTTONS, "")
            .getOrDefaultMDM(hide_buttons)

        drawGrid = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DRAW_GRID, "")
            .getOrDefaultMDM(drawGrid)

        rotation = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROTATION, "")
            .getOrDefaultMDM(rotation)

        autoRotation = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.AUTO_ROTATION, "")
            .getOrDefaultMDM(autoRotation)

        dataSendingInterval = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DATA_SENDING_INTERVAL, "")
            .getOrDefaultMDM(dataSendingInterval)

        dataCollectionInterval = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DATA_COLLECTION_INTERVAL, "")
            .getOrDefaultMDM(dataCollectionInterval)

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

        videofilepaths = MDMService.Preferences
            .get(Const.MDM.SETTING.REMOTE.KEY.VIDEO_FILE_PATHS, "")
            .getOrDefaultMDM(videofilepaths)

        videofileUris = MDMService.Preferences
            .get(Const.MDM.SETTING.REMOTE.KEY.VIDEO_FILE_URIS, "")
            .getOrDefaultMDM(videofileUris)

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

        track_frms = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.TRACK_FRMS, "")
            .getOrDefaultMDM(track_frms)

        ageMode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.AGE_MODE, "")
            .getOrDefaultMDM(ageMode)

        devMode = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEV_MODE, "")
            .getOrDefaultMDM(devMode)

        genderThr = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GENDER_THR, "")
            .getOrDefaultMDM(genderThr)

        use_age_comp = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_AGE_COMP, "")
            .getOrDefaultMDM(use_age_comp)

        use_draw_limit = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.USE_DRAW_LIMIT, "")
            .getOrDefaultMDM(use_draw_limit)

        // =========================
        // ROI (apply)
        // =========================
        roi.apply {

            // TODO: Roi-> json
//            top = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROI_TOP, "")
//                .getOrDefaultMDM(top)
//
//            left = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROI_LEFT, "")
//                .getOrDefaultMDM(left)
//
//            width = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROI_WIDTH, "")
//                .getOrDefaultMDM(width)
//
//            height = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.ROI_HEIGHT, "")
//                .getOrDefaultMDM(height)
        }

        // =========================
        // CamParam (apply)
        // =========================
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

        tvWidth = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.TV_WIDTH, "")
            .getOrDefaultMDM(tvWidth)

        tvHeight = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.TV_HEIGHT, "")
            .getOrDefaultMDM(tvHeight)

        autoStartCameraActivity =
            MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.AUTO_START_CAMERA_ACTIVITY, "")
                .getOrDefaultMDM(autoStartCameraActivity)

        gaApiUrl = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GA_API_URL, "")
            .getOrDefaultMDM(gaApiUrl)

        gaApiSecret = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GA_API_SECRET, "")
            .getOrDefaultMDM(gaApiSecret)

        gaMeasurementId =
            MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.GA_MEASUREMENT_ID, "")
                .getOrDefaultMDM(gaMeasurementId)
    }
}
