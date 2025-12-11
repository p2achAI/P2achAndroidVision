package ai.p2ach.p2achandroidvision.repos.monitoring

import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const

import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.repos.camera.handlers.BaseCameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraInfo
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.utils.AlarmManagerUtil
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.utils.WorkerManagerUtil
import android.content.Context
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicLong
import android.net.TrafficStats
import android.os.Build
import android.os.SystemClock
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.ResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Response
import kotlin.getValue


sealed class MonitoringUiState {
    data object Normal: MonitoringUiState()
    data object AbNormal : MonitoringUiState()
}



data class MonitoringRequest(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("labels")
    val labels: MonitoringLabels,
    @SerializedName("metrics")
    val metrics: MonitoringMetrics
)

data class MonitoringLabels(
    @SerializedName("id")
    val id: String,
    @SerializedName("mode")
    val mode: String,
    @SerializedName("app_type")
    val appType: String,
    @SerializedName("os_version")
    val osVersion: String,
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("app_mode")
    val appMode: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("hw_serial_number")
    val hwSerialNumber: String,
    @SerializedName("manufacturer")
    val manufacturer: String,
    @SerializedName("app_start_time")
    val appStartTime: String,
    @SerializedName("camera_name")
    val cameraName: String,
    @SerializedName("camera_vid")
    val cameraVid: String,
    @SerializedName("camera_pid")
    val cameraPid: String,
    @SerializedName("camera_status")
    val cameraStatus: String,
    @SerializedName("camera_status_log")
    val cameraStatusLog: String,
    @SerializedName("camera_rotation")
    val cameraRotation: Int,
    @SerializedName("camera_flip")
    val cameraFlip: Boolean,
    @SerializedName("camera_resolution")
    val cameraResolution: String
)

data class MonitoringMetrics(
    @SerializedName("app_up_time_sec")
    val appUpTimeSec: Long,
    @SerializedName("camera_health")
    val cameraHealth: Int,
    @SerializedName("temperature")
    val temperature: Float,
    @SerializedName("cpu_usage")
    val cpuUsage: Float,
    @SerializedName("mem_usage")
    val memUsage: Float,
    @SerializedName("app_start_time")
    val appStartTime: String,
    @SerializedName("camera_status")
    val cameraStatus: String,
    @SerializedName("camera_status_log")
    val cameraStatusLog: String,
    @SerializedName("camera_rotation")
    val cameraRotation: Int,
    @SerializedName("camera_flip")
    val cameraFlip: Boolean,
    @SerializedName("camera_resolution")
    val cameraResolution: String,
    @SerializedName("processed_frame_count")
    val processedFrameCount: Long,
    @SerializedName("last_processed_frame_ts")
    val lastProcessedFrameTs: String
)

interface MonitoringApi {
    @POST("device_monitor_server/")
    suspend fun sendHealthCheck(
        @Body body: MonitoringRequest,
    ) : Response<ResponseBody>
}

class MonitoringRepo(private val context : Context) : BaseRepo<Unit, MonitoringApi>(MonitoringApi::class) {


    override fun provideHeaders(): Map<String, String> {
        return mapOf(
            "x-api-key" to BuildConfig.API_KEY,
            "Content-Type" to "application/json",
        )
    }

    companion object {
        private const val TAG = "MonitoringRepo"
        private val MODE =
            if (BuildConfig.FLAVOR.contains("prod")) "production" else "development"

        private val INITIAL_CAMERA_TIMESTAMP = getCurrentDateTime()

        private fun getCurrentDateTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }
    }

    private val startTime = AtomicLong(0)
    private val cpuUsageSamples = mutableListOf<Float>()

    private var cpuStatAvailable = false
    private var readableThermalPath: String? = null

    private lateinit var currentMdm: MDMEntity
    private var currentHandler: BaseCameraHandler? = null

    override fun stream(): Flow<Unit> = emptyFlow()

    private var monitoringAlarmId  : String?=null

    private val _monitorUiState = MutableStateFlow<MonitoringUiState>(MonitoringUiState.AbNormal)
    val monitorUiState : StateFlow<MonitoringUiState>  = _monitorUiState.asStateFlow()


    fun bindHandler(handler: BaseCameraHandler?, mdmEntity: MDMEntity) {
        currentHandler = handler
        currentMdm = mdmEntity

        WorkerManagerUtil.enqueueStartMonitoring(context)
    }




    fun stopMonitoring() {

        monitoringAlarmId?.let {
            AlarmManagerUtil.cancel(context, it)
        }
        monitoringAlarmId = null


    }

    fun startMonitoring(){

        stopMonitoring()
        monitoringAlarmId = AlarmManagerUtil.scheduleInfiniteFromNow(
            context= context,
            Const.ALARM_WOKER.MONITORING.MONITORING_INTERVAL
        ){

            CoroutineExtension.launch {
                var monitoringResult = requestMonitoring()
                if(monitoringResult == null)  setMonitoringStateAbNormal()
                else{
                    if(monitoringResult.isSuccessful) setMonitoringStateNormal()
                    else setMonitoringStateAbNormal()
                }
            }


        }

    }


    private suspend fun requestMonitoring() : Response<ResponseBody>? {


        TrafficStats.setThreadStatsTag(1001)

        cpuStatAvailable = tryReadFile("/proc/stat")
        val thermalCandidates = arrayOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp"
        )
        readableThermalPath = thermalCandidates.firstOrNull { tryReadFile(it) }

        if (!cpuStatAvailable) {
            Log.w(TAG, "CPU Stat is not available")
        }
        if (readableThermalPath == null) {
            Log.w(TAG, "Thermal path is not readable")
        }

        startTime.set(System.currentTimeMillis())

        if (cpuStatAvailable) {

            val usage = getCpuUsagePercent()
            if (usage >= 0) {
                synchronized(cpuUsageSamples) {
                    cpuUsageSamples.add(usage)
                }
            }
        }

        TrafficStats.setThreadStatsTag(0xF00A)

        val service = api ?: return null
        val request = buildRequest() ?: return null

        return  request {sendHealthCheck(request)}


    }


    private fun buildRequest(): MonitoringRequest? {
        val handler = currentHandler ?: run {
            Log.w("Monitoring buildRequest: currentHandler is null")
            return null
        }

        val cameraInfo = handler as? CameraInfo ?: run {
            Log.w("Monitoring buildRequest: handler is not CameraInfo: ${handler::class.java}")
            return null
        }


        val mdm = currentMdm
        if (mdm == null) {
            Log.w("Monitoring buildRequest: currentMdm is null")
            return null
        }

        val deviceName = mdm.deviceName
        val appMode = mdm.featureFlags.appMode

        val reportWallMs = System.currentTimeMillis()
        val deviceUptimeMs = SystemClock.elapsedRealtime()
        val bootEpoch = reportWallMs - deviceUptimeMs
        val lastFrameTsIso = toIsoUtc(
            if (handler.getCameraLastFrameTs() > 0L)
                bootEpoch + handler.getCameraLastFrameTs()
            else 0L
        )



        val avgCpuUsage = synchronized(cpuUsageSamples) {
            if (cpuUsageSamples.isNotEmpty()) {
                "%.2f".format(cpuUsageSamples.average()).toFloat()
            } else {
                -1f
            }
        }
        cpuUsageSamples.clear()



        val labels =
            MonitoringLabels(
                id = deviceName,
                mode = MODE,
                appType = "ai",
                osVersion = "android_sdk_${Build.VERSION.SDK_INT}_(${Build.VERSION.RELEASE})",
                appVersion = BuildConfig.VERSION_NAME,
                appMode = appMode,
                model = Build.MODEL,
                hwSerialNumber = Build.ID,
                manufacturer = Build.MANUFACTURER,
                appStartTime = INITIAL_CAMERA_TIMESTAMP,
                cameraName = cameraInfo.getCameraId(),
                cameraVid = cameraInfo.getCameraVId(),
                cameraPid = cameraInfo.getCameraPId(),
                cameraStatus = cameraInfo.getCameraStatus(),
                cameraStatusLog = cameraInfo.getCameraStatusLog(),
                cameraRotation = handler.getCameraRotation(),
                cameraFlip = handler.getCameraFlip(),
                cameraResolution = cameraInfo.getCameraResolution()
            )



        val metrics =
            MonitoringMetrics(
                appUpTimeSec = getRunningTimeInMinutes(),
                cameraHealth = if (handler.getCameraHealth()) 1 else 0,
                temperature = getCpuTemperature(),
                cpuUsage = avgCpuUsage,
                memUsage = getMemoryUsageInMB().toFloat(),
                appStartTime = INITIAL_CAMERA_TIMESTAMP,
                cameraStatus = cameraInfo.getCameraStatus(),
                cameraStatusLog = cameraInfo.getCameraStatusLog(),
                cameraRotation = handler.getCameraRotation(),
                cameraFlip = handler.getCameraFlip(),
                cameraResolution = cameraInfo.getCameraResolution(),
                processedFrameCount = handler.getCameraProcessedFrameCount(),
                lastProcessedFrameTs = lastFrameTsIso
            )



        return MonitoringRequest(
            deviceId = deviceName,
            labels = labels,
            metrics = metrics
        )
    }



    private fun tryReadFile(path: String): Boolean {
        return try {
            BufferedReader(FileReader(path)).use { }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getRunningTimeInMinutes(): Long {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime.get()
        return elapsedTime / (60 * 1000)
    }

    private fun toIsoUtc(ms: Long): String {
        if (ms <= 0) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ms))
    }

    private fun getCpuTemperature(): Float {
        val path = readableThermalPath ?: return -1f
        return try {
            BufferedReader(FileReader(path)).use { br ->
                val line = br.readLine() ?: return -1f
                val raw = line.toFloatOrNull() ?: return -1f
                if (raw > 200f) raw / 1000f else raw
            }
        } catch (e: Exception) {
            e.printStackTrace()
            -1f
        }
    }

    private fun getMemoryUsageInMB(): Long {
        return try {
            BufferedReader(FileReader("/proc/meminfo")).use { reader ->
                var totalMem = 0L
                var freeMem = 0L
                var cachedMem = 0L
                var buffers = 0L

                repeat(10) {
                    val line = reader.readLine() ?: return@repeat
                    when {
                        line.startsWith("MemTotal:") -> {
                            totalMem = line.split(Regex("\\s+"))[1].toLong()
                        }
                        line.startsWith("MemFree:") -> {
                            freeMem = line.split(Regex("\\s+"))[1].toLong()
                        }
                        line.startsWith("Cached:") -> {
                            cachedMem = line.split(Regex("\\s+"))[1].toLong()
                        }
                        line.startsWith("Buffers:") -> {
                            buffers = line.split(Regex("\\s+"))[1].toLong()
                        }
                    }
                }
                (totalMem - (freeMem + cachedMem + buffers)) / 1024
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun getCpuUsagePercent(): Float {
        if (!cpuStatAvailable) return -1f
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val load = reader.readLine()
            val toks = load.split(" ").filter { it.isNotEmpty() }
            val idle1 = toks[4].toLong()
            val cpu1 = toks.subList(1, 8).sumOf { it.toLong() }
            reader.close()

            Thread.sleep(360)

            val reader2 = BufferedReader(FileReader("/proc/stat"))
            val load2 = reader2.readLine()
            val toks2 = load2.split(" ").filter { it.isNotEmpty() }
            val idle2 = toks2[4].toLong()
            val cpu2 = toks2.subList(1, 8).sumOf { it.toLong() }
            reader2.close()
            ((cpu2 - cpu1 - (idle2 - idle1)) * 100.0f / (cpu2 - cpu1))
        } catch (e: Exception) {
            e.printStackTrace()
            -1f
        }
    }

    private fun setMonitoringState(ms : MonitoringUiState){
        _monitorUiState.value = ms
    }

    fun setMonitoringStateAbNormal(){
        setMonitoringState(MonitoringUiState.AbNormal)
    }

    fun setMonitoringStateNormal(){
        setMonitoringState(MonitoringUiState.Normal)
    }



}


class StartMonitoringWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val monitoringRepo: MonitoringRepo by inject()

    override suspend fun doWork(): Result {
        return try {
            monitoringRepo.startMonitoring()
            Result.success()
        } catch (t: Throwable) {
            Log.d("MonitoringWorker worker failed : ${t.message}")
            Result.retry()
        }
    }
}