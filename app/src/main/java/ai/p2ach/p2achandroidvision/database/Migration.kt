package ai.p2ach.p2achandroidvision.database

import ai.p2ach.p2achandroidvision.Const
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration{

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE table_capture RENAME TO ${Const.DB.TABLE.CAPTURE_REPORT_NAME}")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
//            db.execSQL(
//                """
//            CREATE TABLE IF NOT EXISTS ${Const.DB.TABLE.AI_MODEL_NAME} (
//                binaryPath TEXT NOT NULL,
//                PRIMARY KEY(binaryPath)
//            );
//            """
//            )


        }
    }

}