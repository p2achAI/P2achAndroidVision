package ai.p2ach.p2achandroidvision.repos.mdm


import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseLocalRepo

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.utils.DeviceUtils


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


@Entity(tableName = "table_mdm")
data class MDMEntity(
    @PrimaryKey var deviceName: String = Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME,
    var hwType: String = DeviceUtils.getHwType().name,
    var deviceUuid: String? = null,
    var rtspTimeoutMs: Long = Const.MDM.SETTING.DEFAULT.DEFAULT_RTSP_TIMEOUT_MS,
    var rtspUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_RTSP_URL,
    var apiUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_API_URL,
    var webviewUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_WEBVIEW_URL,
    var middlewareUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_MIDDLEWARE_URL,
    var appMode: String = Const.MDM.SETTING.DEFAULT.DEFAULT_APP_MODE,
    var demo_version: String = Const.MDM.SETTING.DEFAULT.DEFAULT_DEMO_VERSION,
    var broadcast_version: String = Const.MDM.SETTING.DEFAULT.DEFAULT_BROADCAST_VERSION,
    var useSmartSignService: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_SMART_SIGN_SERVICE,
    var hide_buttons: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_HIDE_BUTTONS,
    var drawGrid: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_DRAW_GRID,
    var rotation: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_ROTATION,
    var autoRotation: Boolean = true,
    var dataSendingInterval: Long = Const.MDM.SETTING.DEFAULT.DEFAULT_DATA_SENDING_INTERVAL,
    var dataCollectionInterval: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_DATA_COLLECTION_INTERVAL,
    var useGzip: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_GZIP,
    var use_ota: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_OTA,
    var use_reid: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_REID,
    var use_ageGender_NpuModel: Boolean = true,
    var useVideofile: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_VIDEOFILE,
    var videofilepaths: List<String> = emptyList(),
    var videofileUris: List<String> = emptyList(),
    var use_pose: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_POSE,
    var use_headpose: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_HEADPOSE,
    var use_yolo: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_YOLO,
    var use_par: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_PAR,
    var use_deepsort: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_USE_DEEPSORT,
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
    var corridorRegion: QuadrangleRegion = QuadrangleRegion(),
    var junctionRegion: QuadrangleRegion = QuadrangleRegion(),
    var tvWidth: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TV_WIDTH,
    var tvHeight: Int = Const.MDM.SETTING.DEFAULT.DEFAULT_TV_HEIGHT,
    var autoStartCameraActivity: Boolean = Const.MDM.SETTING.DEFAULT.DEFAULT_AUTO_START_CAMERA_ACTIVITY,

    var gaApiUrl: String = Const.MDM.SETTING.DEFAULT.DEFAULT_GA_API_URL,
    var gaApiSecret: String = "",
    var gaMeasurementId: String = ""
)

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


@Dao
interface MDMDao : BaseDao<MDMEntity> {


    @Query("SELECT EXISTS(SELECT 1 FROM table_mdm)")
    suspend fun existsAny(): Boolean

    @Query("SELECT * FROM table_mdm WHERE deviceName = :deviceName LIMIT 1")
    suspend fun get(deviceName: String): MDMEntity?

    @Query("SELECT * FROM table_mdm WHERE deviceName = 0 LIMIT 1")
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
//                     MDMHandlers(context).init()

                CoroutineScope(Dispatchers.IO).launch {
                    syncMDMInfo()
                }


            }

            override fun onMDMDisconnected() {
                Log.d("onMDMDisconnected")
            }
        })
    }


     suspend fun syncMDMInfo(){


           mdmHandler.init()


            with(db){

                withTransaction {

                    val query = mdmService.queryConfig(BuildConfig.MDM_API_KEY)

                    val mdmDeviceId = query.getString("DEVICE_ID")?:""
                    val deviceName = DeviceUtils.getDeviceName(context,mdmDeviceId)
                    val baseMDMEntity = mdmDao.get(deviceName)?: MDMEntity(deviceName=deviceName)
                    Log.d("baseMDMEntity $baseMDMEntity")
                    val needUpdateMDMEntity = createNeedUpdateMDMEntity(baseMDMEntity)

                    Log.d("syncMDMInfo $query  ${MDMService.Preferences.get("test", "11")}")
//                    Log.d("syncMDMInfo config $configJson ")
//                    mdmDao.upsert(mdmEntity)


                }

            }

    }

    fun createNeedUpdateMDMEntity(baseMDMEntity: MDMEntity) : MDMEntity{

//        val ageGenderModelType = MDMService.Preferences.get("ageGenderModelType", "new")
//          val remoteMdmConfig = MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.MDM_ENTITY,"")
//         Log.d("remoteMdmConfig : $remoteMdmConfig ${ MDMConverters.jsonToMdmEntity(remoteMdmConfig)}")


//
//        MDMService.Preferences.get(Const.MDM.SETTING.REMOTE.KEY.DEVICE_NAME,"").getOrDefaultMDM(
//            Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME)






        return baseMDMEntity



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

