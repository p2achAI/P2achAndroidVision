package ai.p2ach.p2achandroidvision.repos.ai

import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.base.repos.BaseDao
import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.presign.PreSignRepo
import ai.p2ach.p2achandroidvision.utils.CoroutineExtension
import ai.p2ach.p2achandroidvision.utils.DeviceUtils
import ai.p2ach.p2achandroidvision.utils.Log
import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.collections.component1
import kotlin.collections.component2


/*
* 파일 유효성 체크 필요함.
* */

@Entity(tableName = Const.DB.TABLE.AI_MODEL_NAME)
data class AiModelEntity(
    @PrimaryKey val binaryPath: String,
    var isSelected : Boolean? = false
    )

@Dao
interface AiModelDao : BaseDao<AiModelEntity>{

    @Query("SELECT * FROM table_ai_model")
    fun getAllFlow() : Flow<List<AiModelEntity>>


    @Query("SELECT * FROM table_ai_model")
    fun getAllList() : List<AiModelEntity>

}


class AiModelRepo(private val context: Context, private val db: AppDataBase , private val aiModelDao: AiModelDao)
    : BaseRepo<List<AiModelEntity>, Nothing>(), KoinComponent{

    private val preSignRepo : PreSignRepo by inject()

    init {

        CoroutineExtension.launch {
            preSignRepo.downLoadAiModel()
        }

    }


    override fun stream(): Flow<List<AiModelEntity>> = aiModelDao.getAllFlow()

//    suspend fun downloadModelIfNeeded(context: Context, updateStatus: ((String) -> Unit)? = null) {
//
//
//        Log.d("AiModelRepo modelKeys $modelKeys")
//
//        updateStatus?.invoke("Downloading model for SoC: $hwType")
//        withContext(Dispatchers.IO) {
//            try {
//                val fileUrls = modelKeys.associateWith { fetchPresignedUrl(it) }
//                val total = fileUrls.size
//                fileUrls.entries.withIndex().forEach { (index, entry) ->
//                    val (fileKey, url) = entry
//                    val destFile = File(context.filesDir, fileKey)
//                    destFile.parentFile?.mkdirs()
//                    if (!destFile.exists()) {
//                        URL(url).openStream().use { input ->
//                            FileOutputStream(destFile).use { output -> input.copyTo(output) }
//                        }
//                        updateStatus?.invoke("Downloading models files… (${index + 1} / $total)")
//                    }
//                }
//            } catch (e: Exception) {
//                updateStatus?.invoke("Error downloading model: ${e.message}")
//                Log.e("ModelManager", "Error downloading model: ${e.message}")
//            }
//        }
//    }





}