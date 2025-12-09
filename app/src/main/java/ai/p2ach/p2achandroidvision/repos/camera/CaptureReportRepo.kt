package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.base.repos.BaseDao
import ai.p2ach.p2achandroidvision.base.repos.BaseLocalRepo
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.mdm.CaptureReport
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.presign.PreSignRepo
import ai.p2ach.p2achandroidvision.utils.AlarmManagerUtil
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.utils.WorkerManagerUtil
import ai.p2ach.p2achandroidvision.utils.parseTimeString
import ai.p2ach.p2achandroidvision.utils.saveBitmapAsJpeg
import ai.p2ach.p2achandroidvision.utils.toCalendarDayOfWeek
import android.content.Context
import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
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

@Dao
interface CaptureDao : BaseDao<CaptureReportEntity> {

    @Query("SELECT * FROM ${Const.DB.TABLE.CAPTURE_REPORT_NAME} ORDER BY captureId ASC")
    fun observeAll(): Flow<List<CaptureReportEntity>>

    @Query("DELETE FROM ${Const.DB.TABLE.CAPTURE_REPORT_NAME}")
    suspend fun clearAll()

    @Query("SELECT * FROM ${Const.DB.TABLE.CAPTURE_REPORT_NAME} WHERE isSended = 0 ORDER BY captureId ASC")
    suspend fun getPending(): List<CaptureReportEntity>

    @Query("DELETE FROM ${Const.DB.TABLE.CAPTURE_REPORT_NAME} WHERE isSended = 1")
    suspend fun deleteSended()
}

@Entity(tableName = Const.DB.TABLE.CAPTURE_REPORT_NAME)
data class CaptureReportEntity(
    @PrimaryKey
    var captureId: String,
    var capturePath: String,
    var deviceName: String,
    var isSended: Boolean = false
)

interface CaptureApi

data class CaptureReportStatus(
    val startTime: String? = null,
    val currentCaptureCount: Int = 0,
    val targetCaptureCount: Int = 0,
    val uploadedCount: Int = 0,
    val uploadTargetCount: Int = 0,
    val dayOfWeek: Int? = -1
)

class CaptureReportRepo(
    private val context: Context,
    private val db: AppDataBase,
    private val captureDao: CaptureDao
) : BaseLocalRepo<List<CaptureReportEntity>, CaptureApi>(), KoinComponent {

    private val lastFrameLock = Any()
    private var lastFrame: Bitmap? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var frameCollectJob: Job? = null

    private val alarmIds = mutableListOf<String>()

    private val _captureReportStatuses = MutableStateFlow<List<CaptureReportStatus>>(emptyList())
    val captureReportStatuses: StateFlow<List<CaptureReportStatus>> = _captureReportStatuses

    private val presignRepo: PreSignRepo by inject()

    override fun localFlow(): Flow<List<CaptureReportEntity>> =
        captureDao.observeAll()

    override suspend fun saveLocal(data: List<CaptureReportEntity>) {
        db.withTransaction { captureDao.updateAll(data) }
    }

    override suspend fun clearLocal() {
        db.withTransaction { captureDao.clearAll() }
    }

    /**
     * MDMEntity.captureReports: List<CaptureReport> 기준
     */
    fun bindHandler(handler: BaseCameraHandler?, mdmEntity: MDMEntity) {


        if (handler == null) return
        val reports: List<CaptureReport> = mdmEntity?.captureReports.orEmpty()
        if (reports.isEmpty()) return

        val initialStatuses = reports.map { report ->

                CaptureReportStatus(
                    startTime = report.startTime,
                    currentCaptureCount = 0,
                    targetCaptureCount = report.captureCount ?: 0,
                    uploadedCount = 0,
                    uploadTargetCount = report.captureCount ?: 0,
                    dayOfWeek = report.dayOfWeek.toCalendarDayOfWeek()
                )
        }

        _captureReportStatuses.value = initialStatuses

        frameCollectJob?.cancel()
        frameCollectJob = scope.launch {
            handler.frames.collect { bmp ->
                setFrame(bmp)
            }
        }

        startMultipleAlarms(reports, mdmEntity)
    }

    /**
     * 모든 상태를 0으로 초기화
     */
    fun captureCountInit() {
        updateCaptureReportStatus(
            startTime = null,
            currentCaptureCount = 0,
            targetCaptureCount = 0,
            uploadedCount = 0,
            uploadTargetCount = 0,
            dayOfWeek = null
        )
    }

    /**
     * List<CaptureReport> 기반으로 여러 개의 알람 등록
     */
    private fun startMultipleAlarms(
        reports: List<CaptureReport>,
        mdmEntity: MDMEntity?
    ) {
        // 기존 알람 취소
        alarmIds.forEach { id ->
            AlarmManagerUtil.cancel(context, id)
        }
        alarmIds.clear()

        reports.forEachIndexed { index, report ->
            val (h, m, s) = report.startTime?.parseTimeString() ?: Triple(-1, -1, -1)
            val dayOfWeek = report.dayOfWeek.toCalendarDayOfWeek()

            Log.w(
                "CaptureReport",
                "schedule alarm index=$index ${report.dayOfWeek} $h:$m:$s " +
                        "interval=${report.captureInterval} count=${report.captureCount}"
            )

            val alarmId = AlarmManagerUtil.scheduleAtSpecificTime(
                context = context,
                hourOfDay = h,
                minute = m,
                second = s,
                intervalMillis = report.captureInterval ?: -1L,
                count = report.captureCount ?: -1,
                dayOfWeek = dayOfWeek
            ) {
                captureLastFrameForIndex(index, mdmEntity)
            }

            alarmId?.let { alarmIds.add(it) }
        }
    }

    /**
     * index == null 이면 전체 상태에 적용
     * index != null 이면 해당 인덱스 상태만 갱신
     */
    private fun updateCaptureReportStatus(
        index: Int? = null,
        startTime: String? = null,
        currentCaptureCount: Int? = null,
        targetCaptureCount: Int? = null,
        uploadedCount: Int? = null,
        uploadTargetCount: Int? = null,
        dayOfWeek: Int? = null
    ) {
        val currentList = _captureReportStatuses.value
        if (currentList.isEmpty()) return

        val newList = currentList.mapIndexed { i, status ->
            if (index != null && index != i) {
                status
            } else {
                status.copy(
                    startTime = startTime ?: status.startTime,
                    currentCaptureCount = currentCaptureCount ?: status.currentCaptureCount,
                    targetCaptureCount = targetCaptureCount ?: status.targetCaptureCount,
                    uploadedCount = uploadedCount ?: status.uploadedCount,
                    uploadTargetCount = uploadTargetCount ?: status.uploadTargetCount,
                    dayOfWeek = dayOfWeek ?: status.dayOfWeek
                )
            }
        }

        _captureReportStatuses.value = newList
    }

    private fun setFrame(bitmap: Bitmap?) {
        synchronized(lastFrameLock) {
            lastFrame?.recycle()
            lastFrame = bitmap?.let { it.copy(it.config!!, false) }
        }
    }


    fun captureLastFrameForIndex(index: Int, mdmEntity: MDMEntity?) {
        CoroutineExtension.launch {
            val toSave = synchronized(lastFrameLock) {
                lastFrame?.let { it.copy(it.config!!, false) }
            } ?: return@launch

            val captureFile = toSave.saveBitmapAsJpeg(mdmEntity)
            Log.d("CaptureReport ${captureFile.name} saved")

            val captureReportEntity = CaptureReportEntity(
                deviceName = mdmEntity?.deviceName ?: "",
                captureId = captureFile.name,
                capturePath = captureFile.path
            )

            Log.d("CaptureReport db save $captureReportEntity")
            captureDao.upsert(captureReportEntity)

            val currentStatuses = _captureReportStatuses.value
            if (index in currentStatuses.indices) {
                val status = currentStatuses[index]
                val newCount = status.currentCaptureCount + 1

                updateCaptureReportStatus(
                    index = index,
                    currentCaptureCount = newCount
                )

                Log.d(
                    "CaptureReport",
                    "index=$index captureCount: $newCount / ${status.targetCaptureCount}"
                )

                if (status.targetCaptureCount > 0 && newCount >= status.targetCaptureCount) {
                    Log.d("CaptureReport", "index=$index capture finished, enqueue upload worker")
                    WorkerManagerUtil.enqueueUploadPendingCaptures(context)
                }
            }

            toSave.recycle()
        }
    }


    suspend fun uploadPendingCaptures() {
        var successCount = 0

        db.withTransaction {
            captureDao.deleteSended()
            val pending = captureDao.getPending()
            if (pending.isEmpty()) return@withTransaction

            updateCaptureReportStatus(
                uploadedCount = successCount,
                uploadTargetCount = pending.size
            )

            pending.forEach { captureReports ->
                val file = File(captureReports.capturePath)
                if (!file.exists()) {
                    Log.e("CaptureReport", "file not found: ${captureReports.capturePath}")
                    return@forEach
                }

                val success =  presignRepo.uploadCaptureReportImage(
                    captureReports.captureId,
                    file,
                    captureReports.deviceName
                )

                if (success) {
                    successCount++
                    captureDao.upsert(captureReports.copy(isSended = true))

                    val deleted = file.delete()
                    if (deleted) {
                        Log.d("CaptureReport file deleted: ${file.path}")
                    } else {
                        Log.e("CaptureReport file delete failed: ${file.path}")
                    }

                    updateCaptureReportStatus(
                        uploadedCount = successCount,
                        uploadTargetCount = pending.size
                    )
                }
            }
        }
    }
}

class UploadPendingCaptureReportsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val captureReportRepo: CaptureReportRepo by inject()

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