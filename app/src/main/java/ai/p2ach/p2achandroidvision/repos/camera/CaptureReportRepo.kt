package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidvision.base.repos.BaseDao
import ai.p2ach.p2achandroidvision.base.repos.BaseLocalRepo
import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.presign.PreSignRepo
import ai.p2ach.p2achandroidvision.utils.AlarmManagerUtil
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.WorkerManagerUtil
import ai.p2ach.p2achandroidvision.utils.parseTimeString
import ai.p2ach.p2achandroidvision.utils.saveBitmapAsJpeg

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.Query
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.getValue


@Dao
interface CaptureDao : BaseDao<CaptureReportEntity>{


    @Query("SELECT * FROM table_capture ORDER BY captureId ASC")
    fun observeAll(): Flow<List<CaptureReportEntity>>

    @Query("DELETE FROM table_capture")
    suspend fun clearAll()


    @Query("SELECT * FROM table_capture WHERE isSended = 0 ORDER BY captureId ASC")
    suspend fun getPending(): List<CaptureReportEntity>


    @Query("DELETE FROM table_capture WHERE isSended = 1")
    suspend fun deleteSended()


}


@Entity(tableName = "table_capture")
data class CaptureReportEntity(
    @PrimaryKey
    var captureId : String,
    var capturePath : String,
    var deviceName : String,
    var isSended : Boolean = false
)

interface CaptureApi{

}

data class CaptureReportStatus(
    val startTime: String? = null,
    val currentCaptureCount: Int = 0,
    val targetCaptureCount: Int = 0,
    val uploadedCount: Int = 0,
    val uploadTargetCount: Int = 0
)

class CaptureReportRepo(
    private val context: Context,
    private val db: AppDataBase,
    private val captureDao: CaptureDao,
) : BaseLocalRepo<List<CaptureReportEntity>, CaptureApi>() , KoinComponent  {

    private val lastFrameLock = Any()
    private var lastFrame: Bitmap? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var frameCollectJob: Job? = null
    private var alarmId: String? = null


    private var targetCaptureCount: Int = 0
    private var capturedCount: Int = 0


    private val _status = MutableStateFlow(CaptureReportStatus())
    val status: StateFlow<CaptureReportStatus> = _status



    private val presignRepo : PreSignRepo by inject()

    override fun localFlow(): Flow<List<CaptureReportEntity>> =
        captureDao.observeAll()

    override suspend fun saveLocal(data: List<CaptureReportEntity>) {
        db.withTransaction { captureDao.updateAll(data) }
    }

    override suspend fun clearLocal() {
        db.withTransaction { captureDao.clearAll() }
    }

    fun bindHandler(handler: BaseCameraHandler?, mdmEntity: MDMEntity?) {

        if(handler == null) return

        val captureReport = mdmEntity?.captureReport
        if (captureReport?.startTime.isNullOrEmpty() ||
            captureReport.captureInterval == null
            || captureReport.captureCount == null
            || captureReport.captureInterval == -1L
            || captureReport.captureCount == -1
        ) return


        targetCaptureCount = captureReport.captureCount ?: -1
        capturedCount = 0;

        _status.value = CaptureReportStatus(
            startTime = captureReport.startTime,
            currentCaptureCount = 0,
            targetCaptureCount = targetCaptureCount,
            uploadedCount = 0,
            uploadTargetCount = 0
        )



        frameCollectJob?.cancel()
        frameCollectJob = scope.launch {
            handler.frames.collect { bmp ->
                setFrame(bmp)
            }
        }

        startCaptureReportAlarm(mdmEntity)
    }

    fun captureCountInit() {

        targetCaptureCount = 0;
        capturedCount =0;

        _status.value = _status.value.copy(
            currentCaptureCount = 0,
            targetCaptureCount = 0
        )
    }

    private fun startCaptureReportAlarm(mdmEntity: MDMEntity?) {

        alarmId?.let { AlarmManagerUtil.cancel(context, it) }
        var (h,m,s) =mdmEntity?.captureReport?.startTime?.parseTimeString() ?: Triple(-1,-1,-1)


        Log.w("CaptureReport startCaptureReportAlarm $h : $m : $s start. " +
                "captureInterval -> ${mdmEntity?.captureReport?.captureInterval} " +
                "captureCount -> ${mdmEntity?.captureReport?.captureCount}")


        AlarmManagerUtil.scheduleAtSpecificTime(context,
                hourOfDay = h,
                minute = m,
                second = s,
                intervalMillis = mdmEntity?.captureReport?.captureInterval?:-1L,
                count = mdmEntity?.captureReport?.captureCount?:-1,
                ){

            captureLastFrame(mdmEntity)



        }
    }

    private fun captureEnded(){

        if(targetCaptureCount>0 && capturedCount >= targetCaptureCount){
            scope.launch {
                Log.d("CaptureReport targetCaptureCount : $targetCaptureCount  currCaptureCount $capturedCount capture ended.")
                captureCountInit()
//                uploadPendingCaptures()
                WorkerManagerUtil.enqueueUploadPendingCaptures(context)
            }
        }
    }


    private fun setFrame(bitmap: Bitmap?) {
        synchronized(lastFrameLock) {
            lastFrame?.recycle()
            lastFrame = bitmap?.let { it.copy(it.config!!, false) }
        }
    }

    fun captureLastFrame(mdmEntity: MDMEntity?) {
        CoroutineExtension.launch {
            val toSave = synchronized(lastFrameLock) {
                lastFrame?.let { it.copy(it.config!!, false) }
            } ?: return@launch

            val captureFile = toSave.saveBitmapAsJpeg(mdmEntity)

            Log.d("CaptureReport ${captureFile.name} saved")

            val captureReportEntity = CaptureReportEntity(
                deviceName = mdmEntity?.deviceName?:"",
                captureId = captureFile.name,
                capturePath = captureFile.path)

            Log.d("CaptureReport db save $captureReportEntity")
            captureDao.upsert(captureReportEntity).runCatching {
                capturedCount ++
                _status.value = _status.value.copy(
                    currentCaptureCount = capturedCount,
                    targetCaptureCount = mdmEntity?.captureReport?.captureCount?:0
                )
                captureEnded()
            }

            toSave.recycle()
        }
    }



    suspend fun uploadPendingCaptures() {

        var successCount = 0;

        db.withTransaction {
            captureDao.deleteSended()
            val pending = captureDao.getPending()
            if (pending.isEmpty()) return@withTransaction


            _status.value = _status.value.copy(
                uploadedCount = successCount,
                uploadTargetCount = pending.size
            )

            pending.forEach { captureReports ->
                val file = File(captureReports.capturePath)
                if (!file.exists()) {
                    return@forEach
                }
                val success = runCatching {
                    presignRepo.uploadCaptureReportImage(captureReports.captureId, file , captureReports.deviceName)
                }.getOrElse {
                    Log.e("CaptureReport", "upload failed ${captureReports.captureId}: ${it.message}")
                    false
                }
                if (success) {
                    successCount++

                    captureDao.upsert(captureReports.copy(isSended = true))

                    val deleted = file.delete()
                    if (deleted) {
                        Log.d("CaptureReport file deleted: ${file.path}")
                    } else {
                        Log.e("CaptureReport file delete failed: ${file.path}")
                    }

                    _status.value = _status.value.copy(
                        uploadedCount = successCount,
                        uploadTargetCount = _status.value.uploadTargetCount
                    )
                }
            }

        }


    }

}


class UploadPendingCaptureReportsWorker(
    appContext: Context,
    params: WorkerParameters,
  ) : CoroutineWorker(appContext, params) , KoinComponent{

    private val captureReportRepo : CaptureReportRepo by inject()

    override suspend fun doWork(): Result {
        return try {
            captureReportRepo.uploadPendingCaptures()
            Result.success()
        } catch (t: Throwable) {
            Log.d("CaptureReport worker failed : ${t.message}")
            Result.retry()
        }
    }
}
