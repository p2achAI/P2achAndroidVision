package ai.p2ach.p2achandroidvision.repos.camera

import ai.p2ach.p2achandroidlibrary.base.repos.BaseDao
import ai.p2ach.p2achandroidlibrary.base.repos.BaseLocalRepo
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import android.content.Context
import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull


@Dao
interface CaptureDao : BaseDao<CaptureEntity>{

    @Query("SELECT * FROM table_capture")
    fun observe(): Flow<CaptureEntity?>
    @Query("DELETE FROM table_capture")
    suspend fun clearAll()

}


@Entity(tableName = "table_capture")
data class CaptureEntity(
    @PrimaryKey
    var captureId : String,
    var capturePath : String,
    var captureTime : String,
    var isSended : Boolean = false
)

class CaptureRepo(private val context: Context,  private val db:AppDataBase, private val captureDao : CaptureDao) : BaseLocalRepo<CaptureEntity>(){

    override fun localFlow(): Flow<CaptureEntity> = captureDao.observe().filterNotNull()

    override suspend fun saveLocal(data: CaptureEntity) {
       db.withTransaction { captureDao.upsert(data) }
    }

    override suspend fun clearLocal() {
       db.withTransaction { captureDao.clearAll() }
    }

    override fun stream(): Flow<CaptureEntity> {
        return super.stream()
    }


    fun writeToLocal(bitmap : Bitmap?){

    }

}