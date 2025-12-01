package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseLocalRepo
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.utils.AlarmManagerUtil
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.saveBitmapAsJpeg

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
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

class CaptureRepo(
    private val context: Context,
    private val db: AppDataBase,
    private val captureDao: CaptureDao
) : BaseLocalRepo<List<CaptureEntity>>() {

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
            captureReport?.captureInterval == null || captureReport?.captureCount == null) return

        Log.w("bindHandler ${captureReport}")

        frameCollectJob?.cancel()
        frameCollectJob = scope.launch{
            handler.frames.collect { bmp ->
                setFrame(bmp)
            }
        }

        startCaptureAlarm(mdmEntity)
    }

    fun unbindHandler() {
        frameCollectJob?.cancel()
        frameCollectJob = null
        alarmId?.let { AlarmManagerUtil.cancel(context, it) }
        alarmId = null
    }

    private fun startCaptureAlarm(mdmEntity: MDMEntity?) {
        alarmId?.let { AlarmManagerUtil.cancel(context, it) }

        val startAt = System.currentTimeMillis()
        val interval = 5_000L
        val count = 10

        alarmId = AlarmManagerUtil.scheduleSeries(
            context = context,
            startAtMillis = startAt,

            count = count
        ) {
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