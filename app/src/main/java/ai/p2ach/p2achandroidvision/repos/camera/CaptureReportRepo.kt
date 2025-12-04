package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidvision.base.repos.BaseDao
import ai.p2ach.p2achandroidvision.base.repos.BaseLocalRepo
import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.utils.AlarmManagerUtil
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.parseTimeString
import ai.p2ach.p2achandroidvision.utils.saveBitmapAsJpeg

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.Query
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@Dao
interface CaptureDao : BaseDao<CaptureEntity>{


    @Query("SELECT * FROM table_capture ORDER BY captureId ASC")
    fun observeAll(): Flow<List<CaptureEntity>>

    @Query("DELETE FROM table_capture")
    suspend fun clearAll()

}


@Entity(tableName = "table_capture")
data class CaptureEntity(
    @PrimaryKey
    var captureId : String,
    var capturePath : String,
    var isSended : Boolean = false
)

interface CaptureApi{

}

class CaptureReportRepo(
    private val context: Context,
    private val db: AppDataBase,
    private val captureDao: CaptureDao
) : BaseLocalRepo<List<CaptureEntity>, CaptureApi>() {

    private val lastFrameLock = Any()
    private var lastFrame: Bitmap? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var frameCollectJob: Job? = null
    private var alarmId: String? = null

    override fun localFlow(): Flow<List<CaptureEntity>> =
        captureDao.observeAll()

    override suspend fun saveLocal(data: List<CaptureEntity>) {
        db.withTransaction { captureDao.updateAll(data) }
    }

    override suspend fun clearLocal() {
        db.withTransaction { captureDao.clearAll() }
    }

    fun bindHandler(handler: BaseCameraHandler, mdmEntity: MDMEntity?) {

        val captureReport = mdmEntity?.captureReport
        if(captureReport?.startTime.isNullOrEmpty() ||
            captureReport.captureInterval == null
            || captureReport.captureCount == null
            ||captureReport.captureInterval ==-1L
            ||captureReport.captureCount == -1
            ) return

        Log.w("bindHandler ${captureReport}")

        frameCollectJob?.cancel()
        frameCollectJob = scope.launch{
            handler.frames.collect { bmp ->
                setFrame(bmp)
            }
        }

        startCaptureReportAlarm(mdmEntity)
    }

    fun unbindHandler() {
        frameCollectJob?.cancel()
        frameCollectJob = null
        alarmId?.let { AlarmManagerUtil.cancel(context, it) }
        alarmId = null
    }

    private fun startCaptureReportAlarm(mdmEntity: MDMEntity?) {

        alarmId?.let { AlarmManagerUtil.cancel(context, it) }
        var (h,m,s) =mdmEntity?.captureReport?.startTime?.parseTimeString() ?: Triple(-1,-1,-1)


        Log.w("startCaptureReportAlarm $h : $m : $s start. " +
                "captureInterval -> ${mdmEntity?.captureReport?.captureInterval} " +
                "captureCount -> ${mdmEntity?.captureReport?.captureCount}")


        AlarmManagerUtil.scheduleAtSpecificTime(context,
                hourOfDay = h,
                minute = m,
                second = s,
                intervalMillis = mdmEntity?.captureReport?.captureInterval?:-1L,
                count = mdmEntity?.captureReport?.captureCount?:-1,
                ){

            Log.w("AlarmManagerUtil captureLastFrame ${System.currentTimeMillis()}")
            captureLastFrame()
        }
    }

    private fun setFrame(bitmap: Bitmap?) {
        synchronized(lastFrameLock) {
            lastFrame?.recycle()
            lastFrame = bitmap?.let { it.copy(it.config!!, false) }
        }
    }

    fun captureLastFrame() {
        CoroutineExtension.launch {
            val toSave = synchronized(lastFrameLock) {
                lastFrame?.let { it.copy(it.config!!, false) }
            } ?: return@launch

            val captureFile = toSave.saveBitmapAsJpeg()
//            Log.d("captureLastFrame ${captureFile.path}")

            captureDao.upsert(
                CaptureEntity(
                    captureId = captureFile.name,
                    capturePath = captureFile.path
                )
            )

            toSave.recycle()
        }
    }

}