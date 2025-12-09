package ai.p2ach.p2achandroidvision.repos.ai

import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.base.repos.BaseDao
import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.database.AppDataBase
import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


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
    fun getAll() : Flow<List<AiModelEntity>>?

}


class AiModelRepo(private val context: Context, private val db: AppDataBase ) : BaseRepo<AiModelEntity, Nothing>(){
    override fun stream(): Flow<AiModelEntity> =emptyFlow()
}