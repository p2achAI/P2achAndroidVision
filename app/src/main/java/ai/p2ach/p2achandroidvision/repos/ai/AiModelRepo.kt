package ai.p2ach.p2achandroidvision.repos.ai

import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.base.repos.BaseDao
import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.presign.PreSignRepo
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.DeviceUtils
import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.utils.toVisionSdkSettings
import ai.p2ach.vision.sdk.NativeLib
import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream

@Entity(tableName = Const.DB.TABLE.AI_MODEL_NAME)
data class AiModelEntity(
    @PrimaryKey val binaryPath: String,
    var isSelected: Boolean? = false
)

@Dao
interface AiModelDao : BaseDao<AiModelEntity> {

    @Query("SELECT * FROM table_ai_model")
    fun getAllFlow(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM table_ai_model")
    fun getAllList(): List<AiModelEntity>
}

class AiModelRepo(
    private val context: Context,
    private val db: AppDataBase,
    private val aiModelDao: AiModelDao
) : BaseRepo<List<AiModelEntity>, Nothing>(), KoinComponent {

    private val preSignRepo: PreSignRepo by inject()

    private val mutex = Mutex()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun isReady(): Boolean = _ready.value

    init {

    }

    override fun stream(): Flow<List<AiModelEntity>> = aiModelDao.getAllFlow()

    suspend fun preload(mdmEntity: MDMEntity) {
        ensureNativeReady(
            onStatus = { Log.d(it) },
            onError = { Log.d("AiModelRepo preload error $it") },
            mdmEntity = mdmEntity
        )
    }

    suspend fun ensureNativeReady(
        onStatus: ((String) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        mdmEntity: MDMEntity
    ): Boolean = mutex.withLock {

        if (_ready.value) return true

        try {
            onStatus?.invoke("AiModelRepo ensureNativeReady begin")

//            val modelKeys = DeviceUtils.getRequiredModelKeys()
//            syncDbModelList(modelKeys)

            downloadModelIfNeeded()

            val modelPath = DeviceUtils.getModelPath(context)
            onStatus?.invoke("NativeLib.initialize path=$modelPath")

            val ret = withContext(Dispatchers.Default) {
                NativeLib.initialize(modelPath, mdmEntity.toVisionSdkSettings())
            }

            val ok = ret == 0
            if (ok) {
                NativeLib.processorStatus = NativeLib.ProcessorStatus.Initialized
                _ready.value = true
                onStatus?.invoke("NativeLib initialized")
            } else {
                _ready.value = false
                onStatus?.invoke("NativeLib initialize failed ret=$ret")
            }

            ok
        } catch (t: Throwable) {
            _ready.value = false
            onError?.invoke(t)
            false
        }
    }

    private suspend fun syncDbModelList(modelKeys: List<String>) {
        val existing = aiModelDao.getAllList().map { it.binaryPath }.toSet()
        val toInsert = modelKeys
            .filterNot { existing.contains(it) }
            .map { AiModelEntity(binaryPath = it, isSelected = false) }

        if (toInsert.isEmpty()) return

        db.withTransaction {
            aiModelDao.upsertAll(toInsert)
        }
    }

    suspend fun downloadModelIfNeeded() {
        val modelKeys = DeviceUtils.getRequiredModelKeys()
        val hwType = DeviceUtils.getHwType()

        Log.d("AiModelRepo modelKeys $modelKeys")
        Log.w("Downloading model for SoC: $hwType")

        withContext(Dispatchers.IO) {
            val missingKeys = modelKeys.filter { key ->
                val f = File(context.filesDir, key)
                !f.exists()
            }

            if (missingKeys.isEmpty()) {
                Log.w("All model files already exist")
                return@withContext
            }

            val total = missingKeys.size
            var successCount = 0
            val failed = mutableListOf<String>()

            missingKeys.forEachIndexed { index, fileKey ->
                Log.w("Presign… (${index + 1}/$total)")

                val presign = preSignRepo.requestAiModelDownLoadUrl(fileKey)
                if (presign == null || presign.presigned_url.isBlank()) {
                    failed.add(fileKey)
                    Log.w("Presign failed: $fileKey (${index + 1}/$total)")
                    return@forEachIndexed
                }

                val destFile = File(context.filesDir, fileKey)

                val ok = downloadOneFileWithRetry(
                    url = presign.presigned_url,
                    destFile = destFile,
                    maxRetry = 3
                )

                if (ok) {
                    successCount++
                    Log.w("Downloaded… (${index + 1}/$total)")
                } else {
                    failed.add(fileKey)
                }
            }

            if (failed.isEmpty()) {
                Log.w("Model download completed ($successCount/$total)")
            } else {
                Log.w("Partial success ($successCount/$total). failed=${failed.size}")
                Log.d("AiModelRepo failedKeys $failed")
            }
        }
    }

    private suspend fun downloadOneFileWithRetry(
        url: String,
        destFile: File,
        maxRetry: Int = 3
    ): Boolean {
        destFile.parentFile?.mkdirs()
        val tmp = File(destFile.parentFile, destFile.name + ".part")

        repeat(maxRetry) { attempt ->
            try {
                Log.w("Downloading ${destFile.name} (try ${attempt + 1}/$maxRetry)")

                val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                }

                val code = conn.responseCode
                if (code !in 200..299) {
                    val err = runCatching { conn.errorStream?.bufferedReader()?.readText() }.getOrNull()
                    Log.d("download error code=$code msg=${conn.responseMessage} body=$err")
                    conn.disconnect()
                    throw RuntimeException("HTTP $code ${conn.responseMessage}")
                }

                conn.inputStream.use { input ->
                    FileOutputStream(tmp).use { output ->
                        input.copyTo(output)
                    }
                }

                if (tmp.length() <= 0L) {
                    tmp.delete()
                    throw RuntimeException("Downloaded file is empty")
                }

                if (destFile.exists()) destFile.delete()
                val renamed = tmp.renameTo(destFile)
                if (!renamed) {
                    tmp.copyTo(destFile, overwrite = true)
                    tmp.delete()
                }

                return true
            } catch (e: Throwable) {
                Log.d("download failed file=${destFile.name} attempt=${attempt + 1} error=$e")
                tmp.delete()
                if (attempt < maxRetry - 1) {
                    kotlinx.coroutines.delay((attempt + 1) * 800L)
                }
            }
        }

        Log.w("Failed ${destFile.name}")
        return false
    }
}