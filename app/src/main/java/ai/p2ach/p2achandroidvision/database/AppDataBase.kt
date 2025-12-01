package ai.p2ach.p2achandroidvision.database

import ai.p2ach.p2achandroidvision.repos.camera.CaptureDao
import ai.p2ach.p2achandroidvision.repos.camera.CaptureEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMDao
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMConverters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MDMEntity::class, CaptureEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(MDMConverters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun MDMDao(): MDMDao
    abstract fun CaptureDao() : CaptureDao
}