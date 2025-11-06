package ai.p2ach.p2achandroidvision.repos


import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.database.AppDataBase
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable


@Entity(
    tableName = "table_mdm_setting"
)
data class MDMSettingEntity (
    var deviceName : String = Const.MDM.SETTING.DEFAULT_DEVICE_NAME,
    var hwType : String = Const.MDM.SETTING.DEFAULT_HW_TYPE,
    var deviceUuid : String? = null,
    var rtspTimeoutMs: Long = Const.MDM.SETTING.DEFAULT_RTSP_TIMEOUT_MS,
    var rtspUrl: String = Const.MDM.SETTING.DEFAULT_RTSP_URL,
    var apiUrl: String = Const.MDM.SETTING.DEFAULT_API_URL,
    var webviewUrl: String = Const.MDM.SETTING.DEFAULT_WEBVIEW_URL,
    var middlewareUrl: String = Const.MDM.SETTING.DEFAULT_MIDDLEWARE_URL,
    var appMode: String = Const.MDM.SETTING.DEFAULT_APP_MODE,
    var demo_version: String = Const.MDM.SETTING.DEFAULT_DEMO_VERSION,
    var broadcast_version : String = Const.MDM.SETTING.DEFAULT_BROADCAST_VERSION,
    var useSmartSignService: Boolean = Const.MDM.SETTING.DEFAULT_USE_SMART_SIGN_SERVICE,
    var hide_buttons: Boolean = Const.MDM.SETTING.DEFAULT_HIDE_BUTTONS,
    var drawGrid: Boolean = Const.MDM.SETTING.DEFAULT_DRAW_GRID,
    var rotation: Int = Const.MDM.SETTING.DEFAULT_ROTATION,      // rotation 1 => 90 CLOCKWISE , rotation 2 => 180 , rotation 3 => 270 CLOCKWISE (= 90 COUNTERCLOCKWISE)
    var autoRotation: Boolean = true,                       // 기기 방향에 따라서 자동으로 화면 회전 보정을 적용할 것인지
    var dataSendingInterval: Long = Const.MDM.SETTING.DEFAULT_DATA_SENDING_INTERVAL,
    var dataCollectionInterval: Int = Const.MDM.SETTING.DEFAULT_DATA_COLLECTION_INTERVAL,
    var useGzip: Boolean = Const.MDM.SETTING.DEFAULT_USE_GZIP,
    var use_ota: Boolean = Const.MDM.SETTING.DEFAULT_USE_OTA,
    var use_reid: Boolean = Const.MDM.SETTING.DEFAULT_USE_REID,
    var use_ageGender_NpuModel: Boolean = true,
    var useVideofile: Boolean = Const.MDM.SETTING.DEFAULT_USE_VIDEOFILE,
    var videofilepaths: List<String> = emptyList(),
    var videofileUris: List<String> = emptyList(),
    var use_pose: Boolean = Const.MDM.SETTING.DEFAULT_USE_POSE,
    var use_headpose: Boolean = Const.MDM.SETTING.DEFAULT_USE_HEADPOSE,
    var use_yolo: Boolean = Const.MDM.SETTING.DEFAULT_USE_YOLO,
    var use_par: Boolean = Const.MDM.SETTING.DEFAULT_USE_PAR,
    var use_deepsort: Boolean = Const.MDM.SETTING.DEFAULT_USE_DEEPSORT,
    var use_face: Boolean = Const.MDM.SETTING.DEFAULT_USE_FACE,
    var use_4split: Boolean = Const.MDM.SETTING.DEFAULT_USE_4SPLIT,
    var contents_mode: Boolean = Const.MDM.SETTING.DEFAULT_CONTENTS_MODE,
    var flip: Boolean = Const.MDM.SETTING.DEFAULT_FLIP,
    var track_frms: Int = Const.MDM.SETTING.DEFAULT_TRACK_FRMS,
    var ageMode: Int = Const.MDM.SETTING.DEFAULT_AGE_MODE,
    var devMode: Boolean = Const.MDM.SETTING.DEFAULT_DEV_MODE,
    var genderThr: Float = Const.MDM.SETTING.DEFAULT_GENDER_THR,
    var use_age_comp: Boolean = Const.MDM.SETTING.DEFAULT_USE_AGE_COMP,
    var use_draw_limit: Boolean = Const.MDM.SETTING.DEFAULT_USE_DRAW_LIMIT,
    var roi : ROI = ROI(),
    var camParam: CamParam = CamParam(),
    var corridorRegion: QuadrangleRegion = QuadrangleRegion(),
    var junctionRegion: QuadrangleRegion = QuadrangleRegion(),
    var tvWidth : Int = Const.MDM.SETTING.DEFAULT_TV_WIDTH,
    var tvHeight : Int = Const.MDM.SETTING.DEFAULT_TV_HEIGHT,
    var autoStartCameraActivity: Boolean = Const.MDM.SETTING.DEFAULT_AUTO_START_CAMERA_ACTIVITY,  // LoadingActivity에서 CameraActivity 자동 실행 여부
    var gaApiUrl: String = Const.MDM.SETTING.DEFAULT_GA_API_URL,
    var gaApiSecret: String = "",
    var gaMeasurementId: String = ""
)

@Dao
interface MDMSettingDAO : BaseDao<MDMSettingEntity> {
    @Query("SELECT * FROM table_mdm_setting")
    fun observeAll(): Flow<List<MDMSettingEntity>>
}




@Serializable
data class ROI (
    var top : Int = 0,
    var left : Int = 0,
    var width : Int = 0,
    var height : Int = 0
)


@Serializable
data class CamParam (
    var focal_x : Float = Const.MDM.SETTING.DEFAULT_FOCAL_X,
    var focal_y : Float = Const.MDM.SETTING.DEFAULT_FOCAL_Y,
    var rot_x : Float = 0F,
    var rot_y : Float = 0F,
    var rot_z : Float = 1F,
    var did_w : Float = Const.MDM.SETTING.DEFAULT_DID_W,
    var did_h : Float = Const.MDM.SETTING.DEFAULT_DID_H,
    var did_l : Float = Const.MDM.SETTING.DEFAULT_DID_L,
    var did_t : Float = Const.MDM.SETTING.DEFAULT_DID_T,
    var scale1 : Float = Const.MDM.SETTING.DEFAULT_SCALE1,
    var scale2 : Float = Const.MDM.SETTING.DEFAULT_SCALE2
)


@Serializable
data class QuadrangleRegion(
    var leftLine: QuadrangleLine = QuadrangleLine(),
    var rightLine: QuadrangleLine = QuadrangleLine()
) {
    fun isValid(): Boolean {
        return leftLine.startX > 0 && leftLine.startY > 0 &&
                leftLine.endX > 0 && leftLine.endY > 0 &&
                rightLine.startX > 0 && rightLine.startY > 0 &&
                rightLine.endX > 0 && rightLine.endY > 0
    }
}



@Serializable
data class QuadrangleLine(
    var startX: Float = -1F,
    var startY: Float = -1F,
    var endX: Float = -1F,
    var endY: Float = -1F
)

class MDMRepo(private val db: AppDataBase): BaseRepo<MDMSettingEntity>(){

    override fun localFlow(): Flow<MDMSettingEntity> {

        return emptyFlow()
    }

    override suspend fun saveLocal(data: MDMSettingEntity) {


    }

    override suspend fun clearLocal() {

    }


}