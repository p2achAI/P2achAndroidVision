package ai.p2ach.p2achandroidvision.repos


import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseRepo
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.database.AppDataBase
import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.withTransaction
import com.hmdm.MDMService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.filterNotNull


@Entity(tableName = "table_mdm")
data class MDMEntity(
    @PrimaryKey val deviceName: String = Const.MDM.SETTING.DEFAULT_DEVICE_NAME,
    val hwType: String = Const.MDM.SETTING.DEFAULT_HW_TYPE,
    val deviceUuid: String? = null,
    val rtspTimeoutMs: Long = Const.MDM.SETTING.DEFAULT_RTSP_TIMEOUT_MS,
    val rtspUrl: String = Const.MDM.SETTING.DEFAULT_RTSP_URL,
    val apiUrl: String = Const.MDM.SETTING.DEFAULT_API_URL,
    val webviewUrl: String = Const.MDM.SETTING.DEFAULT_WEBVIEW_URL,
    val middlewareUrl: String = Const.MDM.SETTING.DEFAULT_MIDDLEWARE_URL,
    val appMode: String = Const.MDM.SETTING.DEFAULT_APP_MODE,
    val demo_version: String = Const.MDM.SETTING.DEFAULT_DEMO_VERSION,
    val broadcast_version: String = Const.MDM.SETTING.DEFAULT_BROADCAST_VERSION,
    val useSmartSignService: Boolean = Const.MDM.SETTING.DEFAULT_USE_SMART_SIGN_SERVICE,
    val hide_buttons: Boolean = Const.MDM.SETTING.DEFAULT_HIDE_BUTTONS,
    val drawGrid: Boolean = Const.MDM.SETTING.DEFAULT_DRAW_GRID,
    val rotation: Int = Const.MDM.SETTING.DEFAULT_ROTATION,
    val autoRotation: Boolean = true,
    val dataSendingInterval: Long = Const.MDM.SETTING.DEFAULT_DATA_SENDING_INTERVAL,
    val dataCollectionInterval: Int = Const.MDM.SETTING.DEFAULT_DATA_COLLECTION_INTERVAL,
    val useGzip: Boolean = Const.MDM.SETTING.DEFAULT_USE_GZIP,
    val use_ota: Boolean = Const.MDM.SETTING.DEFAULT_USE_OTA,
    val use_reid: Boolean = Const.MDM.SETTING.DEFAULT_USE_REID,
    val use_ageGender_NpuModel: Boolean = true,
    val useVideofile: Boolean = Const.MDM.SETTING.DEFAULT_USE_VIDEOFILE,
    val videofilepaths: List<String> = emptyList(),
    val videofileUris: List<String> = emptyList(),
    val use_pose: Boolean = Const.MDM.SETTING.DEFAULT_USE_POSE,
    val use_headpose: Boolean = Const.MDM.SETTING.DEFAULT_USE_HEADPOSE,
    val use_yolo: Boolean = Const.MDM.SETTING.DEFAULT_USE_YOLO,
    val use_par: Boolean = Const.MDM.SETTING.DEFAULT_USE_PAR,
    val use_deepsort: Boolean = Const.MDM.SETTING.DEFAULT_USE_DEEPSORT,
    val use_face: Boolean = Const.MDM.SETTING.DEFAULT_USE_FACE,
    val use_4split: Boolean = Const.MDM.SETTING.DEFAULT_USE_4SPLIT,
    val contents_mode: Boolean = Const.MDM.SETTING.DEFAULT_CONTENTS_MODE,
    val flip: Boolean = Const.MDM.SETTING.DEFAULT_FLIP,
    val track_frms: Int = Const.MDM.SETTING.DEFAULT_TRACK_FRMS,
    val ageMode: Int = Const.MDM.SETTING.DEFAULT_AGE_MODE,
    val devMode: Boolean = Const.MDM.SETTING.DEFAULT_DEV_MODE,
    val genderThr: Float = Const.MDM.SETTING.DEFAULT_GENDER_THR,
    val use_age_comp: Boolean = Const.MDM.SETTING.DEFAULT_USE_AGE_COMP,
    val use_draw_limit: Boolean = Const.MDM.SETTING.DEFAULT_USE_DRAW_LIMIT,
    val roi: ROI = ROI(),
    val camParam: CamParam = CamParam(),
    val corridorRegion: QuadrangleRegion = QuadrangleRegion(),
    val junctionRegion: QuadrangleRegion = QuadrangleRegion(),
    val tvWidth: Int = Const.MDM.SETTING.DEFAULT_TV_WIDTH,
    val tvHeight: Int = Const.MDM.SETTING.DEFAULT_TV_HEIGHT,
    val autoStartCameraActivity: Boolean = Const.MDM.SETTING.DEFAULT_AUTO_START_CAMERA_ACTIVITY,
    val gaApiUrl: String = Const.MDM.SETTING.DEFAULT_GA_API_URL,
    val gaApiSecret: String = "",
    val gaMeasurementId: String = ""
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


@Dao
interface MDMDao : BaseDao<MDMEntity> {
    @Query("SELECT * FROM table_mdm WHERE deviceName = 0 LIMIT 1")
    fun observe(): Flow<MDMEntity?>

    @Query("DELETE FROM table_mdm")
    suspend fun clearAll()

}

class MDMRepo(private val context: Context, private val db: AppDataBase, private val MDMDao: MDMDao): BaseRepo<MDMEntity>(){

    init {


        Log.d("MDMRepo init")


            MDMService.getInstance().connect(context, object : MDMService.ResultHandler {

                override fun onMDMConnected() {
                    Log.d("onMDMConnected")
                }

                override fun onMDMDisconnected() {
                    Log.d("onMDMDisconnected")
                }
            })

    }


    override fun localFlow(): Flow<MDMEntity> =
        MDMDao.observe().filterNotNull()

    override suspend fun saveLocal(data: MDMEntity) {
        db.withTransaction { MDMDao.upsert(data) }
    }

    override suspend fun clearLocal() {
        db.withTransaction { MDMDao.clearAll() }
    }

    suspend fun replaceAll(value: MDMEntity) {
        db.withTransaction {
            MDMDao.clearAll()
            MDMDao.upsert(value)
        }
    }
}

