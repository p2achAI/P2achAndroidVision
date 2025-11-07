package ai.p2ach.p2achandroidvision.database

import ai.p2ach.p2achandroidvision.repos.MDMDao
import ai.p2ach.p2achandroidvision.repos.MDMEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MDMEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(MDMConverters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun MDMDao(): MDMDao
}