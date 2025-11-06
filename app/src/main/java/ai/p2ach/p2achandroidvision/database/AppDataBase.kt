package ai.p2ach.p2achandroidvision.database

import ai.p2ach.p2achandroidvision.repos.MDMSettingDAO
import ai.p2ach.p2achandroidvision.repos.MDMSettingEntity
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MDMSettingEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDataBase : RoomDatabase() {
    abstract fun mdmSettingDao(): MDMSettingDAO
}