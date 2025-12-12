package ai.p2ach.p2achandroidvision.database


import ai.p2ach.p2achandroidvision.repos.ai.AiModelDao
import ai.p2ach.p2achandroidvision.repos.ai.AiModelEntity
import ai.p2ach.p2achandroidvision.repos.camera.CaptureDao
import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMDao
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMConverters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MDMEntity::class, CaptureReportEntity::class, AiModelEntity::class ],
    version = 2,
    exportSchema = false
)
@TypeConverters(MDMConverters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun MDMDao(): MDMDao
    abstract fun CaptureDao() : CaptureDao

    abstract fun AiModelDao() : AiModelDao
}


