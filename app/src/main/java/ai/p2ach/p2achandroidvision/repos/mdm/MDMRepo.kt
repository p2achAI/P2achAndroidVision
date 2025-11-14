package ai.p2ach.p2achandroidvision.repos.mdm


import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseLocalRepo

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.utils.DeviceUtils
import ai.p2ach.p2achandroidvision.utils.getNeedUpdateMDMEntity


import android.content.Context

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.withTransaction
import com.hmdm.MDMService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID




@Serializable
data class NetWorkAndApi(
    var rtspUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_RTSP_URL,
    var rtspTimeoutMs: Long = Const.MDM.SETTING.DEFAULT.DEFAULT_RTSP_TIMEOUT_MS,
    var apiUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_API_URL,
    var webviewUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_WEBVIEW_URL,
    var middlewareUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_MIDDLEWARE_URL,
    var localWebviewUrl : String = Const.MDM.SETTING.DEFAULT.DEFAULT_LOCAL_WEBVIEW_URL,
)


@Serializable
data class Versions(
    var demo_version: String = Const.MDM.SETTING.DEFAULT.DEFAULT_DEMO_VERSION,
    var broadcast_version: String = Const.MDM.SETTING.DEFAULT.DEFAULT_BROADCAST_VERSION,
)


@Entity(tableName = "table_mdm")
data class MDMEntity(
    @PrimaryKey var deviceName: String = Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME,
    var hwType: String = DeviceUtils.getHwType().name,
    var deviceUuid: String? = null,
    var netWorkAndApi: NetWorkAndApi = NetWorkAndApi(),
    var versions : Versions = Versions(),
    var appMode: String = Const.MDM.SETTING.DEFAULT.DEFAULT_APP_MODE,


    var useSmartSignService: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_SMART_SIGN_SERVICE,
    var hide_buttons: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_HIDE_BUTTONS,
    var drawGrid: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_DRAW_GRID,
    var rotation: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_ROTATION,
    var autoRotation: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_AUTO_ROTATION,
    var dataSendingInterval: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_DATA_SENDING_INTERVAL,
    var dataCollectionInterval: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_DATA_COLLECTION_INTERVAL,
    var useGzip: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_GZIP,
    var use_ota: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_OTA,
    var use_reid: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_REID,
    var use_ageGender_NpuModel: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_AGE_GENDER_NPU_MODEL,
    var useVideofile: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_VIDEO_FILE,
    var videofilepaths: List<String> = emptyList(),
    var videofileUris: List<String> = emptyList(),
    var use_pose: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_POSE,
    var use_headpose: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_HEADPOSE,
    var use_yolo: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_YOLO,
    var use_par: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_PAR,
    var use_deepsort: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_DEEP_SORT,
    var use_face: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_FACE,
    var use_4split: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_4SPLIT,
    var contents_mode: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_CONTENTS_MODE,
    var flip: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_FLIP,
    var track_frms: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TRACK_FRMS,
    var ageMode: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_AGE_MODE,
    var devMode: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_DEV_MODE,
    var genderThr: Float = Const.MDM.SETTING.DEFAULT.DEFAULT_GENDER_THR,
    var use_age_comp: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_AGE_COMP,
    var use_draw_limit: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_DRAW_LIMIT,
    var roi: ROI = ROI(),
    var camParam: CamParam = CamParam(),
    var tvWidth: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TV_WIDTH,
    var tvHeight: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TV_HEIGHT,
    var autoStartCameraActivity: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_AUTO_START_CAMERA_ACTIVITY,

    var gaApiUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_GA_API_URL,
    var gaApiSecret: String = "",
    var gaMeasurementId: String = "",


    var exposure: Exposure = Exposure()

){

    override fun toString(): String {

        return buildString {
            appendLine("deviceName=$deviceName")
            appendLine("hwType=$hwType")
            appendLine("deviceUuid=$deviceUuid")
            appendLine("rtspTimeoutMs=${netWorkAndApi.rtspTimeoutMs}")
            appendLine("rtspUrl=${netWorkAndApi.rtspUrl}l")
            appendLine("apiUrl=$${netWorkAndApi.apiUrl}")
            appendLine("webviewUrl=$${netWorkAndApi.webviewUrl}")
            appendLine("localWebviewUrl=$${netWorkAndApi.localWebviewUrl}")
            appendLine("middlewareUrl=$${netWorkAndApi.middlewareUrl}")
            appendLine("appMode=$appMode")
            appendLine("demo_version=${versions.demo_version}")
            appendLine("broadcast_version=${versions.broadcast_version}")
            appendLine("useSmartSignService=$useSmartSignService")
            appendLine("hide_buttons=$hide_buttons")
            appendLine("drawGrid=$drawGrid")
            appendLine("rotation=$rotation")
            appendLine("autoRotation=$autoRotation")
            appendLine("dataSendingInterval=$dataSendingInterval")
            appendLine("dataCollectionInterval=$dataCollectionInterval")
            appendLine("useGzip=$useGzip")
            appendLine("use_ota=$use_ota")
            appendLine("use_reid=$use_reid")
            appendLine("use_ageGender_NpuModel=$use_ageGender_NpuModel")
            appendLine("useVideofile=$useVideofile")
            appendLine("videofilepaths=$videofilepaths")
            appendLine("videofileUris=$videofileUris")
            appendLine("use_pose=$use_pose")
            appendLine("use_headpose=$use_headpose")
            appendLine("use_yolo=$use_yolo")
            appendLine("use_par=$use_par")
            appendLine("use_deepsort=$use_deepsort")
            appendLine("use_face=$use_face")
            appendLine("use_4split=$use_4split")
            appendLine("contents_mode=$contents_mode")
            appendLine("flip=$flip")
            appendLine("track_frms=$track_frms")
            appendLine("ageMode=$ageMode")
            appendLine("devMode=$devMode")
            appendLine("genderThr=$genderThr")
            appendLine("use_age_comp=$use_age_comp")
            appendLine("use_draw_limit=$use_draw_limit")
            appendLine("roi=$roi")
            appendLine("camParam=$camParam")
            appendLine("tvWidth=$tvWidth")
            appendLine("tvHeight=$tvHeight")
            appendLine("autoStartCameraActivity=$autoStartCameraActivity")
            appendLine("gaApiUrl=$gaApiUrl")
            appendLine("gaApiSecret=$gaApiSecret")
            appendLine("gaMeasurementId=$gaMeasurementId")
            appendLine("exposure=$exposure")
        }
    }

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
    var focal_x : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_FOCAL_X,
    var focal_y : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_FOCAL_Y,
    var rot_x : Float = 0F,
    var rot_y : Float = 0F,
    var rot_z : Float = 1F,
    var did_w : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_DID_W,
    var did_h : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_DID_H,
    var did_l : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_DID_L,
    var did_t : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_DID_T,
    var scale1 : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_SCALE1,
    var scale2 : Float = Const.MDM.SETTING.DEFAULT.DEFAULT_SCALE2
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

@Serializable
data class Exposure(
    var p5: Float = 10.0f,
    var p95: Float = 235.0f,
)




@Dao
interface MDMDao : BaseDao<MDMEntity> {


    @Query("SELECT EXISTS(SELECT 1 FROM table_mdm)")
    suspend fun existsAny(): Boolean

    @Query("SELECT * FROM table_mdm  LIMIT 1")
    suspend fun get(): MDMEntity?

    @Query("SELECT * FROM table_mdm LIMIT 1")
    fun observe(): Flow<MDMEntity?>

    @Query("DELETE FROM table_mdm")
    suspend fun clearAll()

}

class MDMRepo(private val context: Context, private val db: AppDataBase, private val mdmDao: MDMDao): BaseLocalRepo<MDMEntity>(), KoinComponent{

    private val mdmHandler : MDMHandlers by inject()
    private var mdmService = MDMService.getInstance()



    init {

        mdmService.connect(context, object : MDMService.ResultHandler {

            override fun onMDMConnected() {
                Log.d("onMDMConnected")

                CoroutineScope(Dispatchers.IO).launch {
                    mdmHandler.init()
                    syncMDMInfo()
                }


            }

            override fun onMDMDisconnected() {
                Log.d("onMDMDisconnected")
            }
        })
    }


     suspend fun syncMDMInfo(){



            with(db){

                withTransaction {

                    val query = mdmService.queryConfig(BuildConfig.MDM_API_KEY)

                    val mdmDeviceId = query.getString("DEVICE_ID")?:""
                    val deviceName = DeviceUtils.getDeviceName(context,mdmDeviceId)
                    val baseMDMEntity = mdmDao.get()?: MDMEntity(deviceName=deviceName)
                    saveLocal(getNeedUpdateMDMEntity(baseMDMEntity))


                }

            }

    }



    override fun localFlow(): Flow<MDMEntity> =
        mdmDao.observe().filterNotNull()

    override suspend fun saveLocal(data: MDMEntity) {
        db.withTransaction { mdmDao.upsert(data) }
    }

    override suspend fun clearLocal() {
        db.withTransaction { mdmDao.clearAll() }
    }

    suspend fun replaceAll(value: MDMEntity) {
        db.withTransaction {
            mdmDao.clearAll()
            mdmDao.upsert(value)
        }
    }
}

