package com.project.locarm.data.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    private val currentTime = System.currentTimeMillis()
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE AddressRemoteKey
                ADD COLUMN updateAt INTEGER NOT NULL DEFAULT $currentTime
                """.trimIndent()
            )
        }
    }
}
