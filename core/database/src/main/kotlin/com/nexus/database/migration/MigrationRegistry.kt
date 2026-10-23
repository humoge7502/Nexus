package com.nexus.database.migration

import androidx.room.migration.Migration
import com.nexus.database.MIGRATION_1_2
import com.nexus.database.MIGRATION_2_3
import com.nexus.database.MIGRATION_3_4
import com.nexus.database.MIGRATION_4_5
import com.nexus.database.MIGRATION_5_6
import com.nexus.database.MIGRATION_6_7
import com.nexus.database.MIGRATION_7_8

object MigrationRegistry {

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
    )

    val LATEST_VERSION: Int
        get() = ALL_MIGRATIONS.lastOrNull()?.endVersion
            ?: throw IllegalStateException("No migrations registered")

    fun getMigrationsUpTo(version: Int): Array<Migration> {
        return ALL_MIGRATIONS.filter { it.endVersion <= version }.toTypedArray()
    }
}
