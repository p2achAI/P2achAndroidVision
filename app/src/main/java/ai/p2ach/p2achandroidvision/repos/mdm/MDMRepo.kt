package ai.p2ach.p2achandroidvision.repos.mdm


import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseLocalRepo

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraType
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject




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


@Serializable
data class FeatureFlags(
    var appMode: String = Const.MDM.SETTING.DEFAULT.DEFAULT_APP_MODE,
    var useSmartSignService: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_SMART_SIGN_SERVICE,
    var hide_buttons: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_HIDE_BUTTONS,
    var drawGrid: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_DRAW_GRID,
    var autoRotation: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_AUTO_ROTATION,
    var useGzip: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_GZIP,
    var use_ota: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_OTA,
    var use_reid: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_REID,
    var use_ageGender_NpuModel: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_AGE_GENDER_NPU_MODEL,
    var useVideofile: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_VIDEO_FILE,
    var use_pose: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_POSE,
    var use_headpose: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_HEADPOSE,
    var use_yolo: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_YOLO,
    var use_par: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_PAR,
    var use_deepsort: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_DEEP_SORT,
    var use_face: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_FACE,
    var use_4split: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_4SPLIT,
    var contents_mode: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_CONTENTS_MODE,
    var flip: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_FLIP,
    var devMode: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_DEV_MODE,
    var use_age_comp: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_AGE_COMP,
    var use_draw_limit: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_DRAW_LIMIT,
    var autoStartCameraActivity: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_AUTO_START_CAMERA_ACTIVITY,
)


@Serializable
data class TimingsAndParameters(
    var dataSendingInterval: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_DATA_SENDING_INTERVAL,
    var dataCollectionInterval: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_DATA_COLLECTION_INTERVAL,
    var track_frms: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TRACK_FRMS,
    var ageMode: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_AGE_MODE,
    var genderThr: Float = Const.MDM.SETTING.DEFAULT.DEFAULT_GENDER_THR,
)


@Serializable
data class TopView(
    var tvWidth: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TV_WIDTH,
    var tvHeight: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TV_HEIGHT,
)
@Serializable
data class Ga(
    var gaApiUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_GA_API_URL,
    var gaApiSecret: String = "",
    var gaMeasurementId: String = "",
)


@Serializable
data class Testing(
    var videofilepaths: List<String> = emptyList(),
    var videofileUris: List<String> = emptyList(),
)



@Entity(tableName = "table_mdm")
data class MDMEntity(
    @PrimaryKey var deviceName: String = Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME,
    var cameraType:String= Const.MDM.SETTING.DEFAULT.DEFAULT_CAMERA_TYPE,
    var hwType: String = DeviceUtils.getHwType().name,
    var deviceUuid: String? = null,
    var netWorkAndApi: NetWorkAndApi = NetWorkAndApi(),
    var versions : Versions = Versions(),
    var featureFlags: FeatureFlags = FeatureFlags(),
    var timingsAndParameters: TimingsAndParameters = TimingsAndParameters(),
    var roi: ROI = ROI(),
    var camParam: CamParam = CamParam(),
    var topView: TopView = TopView(),
    var ga : Ga = Ga(),
    var exposure: Exposure = Exposure(),
    var testing: Testing = Testing(),
    var rotation: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_ROTATION,

    ){


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
    private var mdmConnected = false


    init {

        mdmService.connect(context, object : MDMService.ResultHandler {

            override fun onMDMConnected() {
                CoroutineScope(Dispatchers.IO).launch {
                    mdmConnected = true
                    mdmHandler.init()
                    syncMDMInfo()
                }
            }

            override fun onMDMDisconnected() {
                Log.d("onMDMDisconnected")
            }
        })

        CoroutineScope(Dispatchers.IO).launch {
            delay(5000)
            if (!mdmConnected) {
                Log.d("MDMService not available → fallback to local default")

                upsertLocalDefault()
            }
        }

    }

    private suspend fun upsertLocalDefault() {
        val exists = mdmDao.existsAny()
        if (!exists) {
            val deviceName = DeviceUtils.getDeviceName(context, "")
            val defaultEntity = MDMEntity(deviceName = deviceName, cameraType = CameraType.INTERNAL.name)
            saveLocal(defaultEntity)
        }
    }



     suspend fun syncMDMInfo(){



            with(db){

                withTransaction {

                    val query = mdmService.queryConfig(BuildConfig.MDM_API_KEY)

                    val mdmDeviceId = query.getString("DEVICE_ID")?:""
                    val deviceName = DeviceUtils.getDeviceName(context,mdmDeviceId)
                    val baseMDMEntity = mdmDao.get()?: MDMEntity(deviceName=deviceName)
                    Log.d("baseMDMEntity $baseMDMEntity")
                    saveLocal(getNeedUpdateMDMEntity(baseMDMEntity))

                    /*appMode 에 따라 화면 구성을 다르게 하거나, 기능적으로 조합을 해야하는 기능이 있다 */
                    /*카메라 모드를 명시적으로 예) uvc, rtsp가 모두 붙어있을때 선택 화면이라던지 pref 구성*/
                    /*로그 dataDog / firebase 확인.*/



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

