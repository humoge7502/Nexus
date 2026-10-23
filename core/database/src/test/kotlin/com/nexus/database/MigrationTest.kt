package com.nexus.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NexusDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 8, true,
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8
        )
    }

    @Test
    @Throws(IOException::class)
    fun migrate1to2PreservesData() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO internships (id, title, company_name, location, is_remote, duration_months, apply_url, cached_at) VALUES ('1', 'Test', 'Test Corp', 'Remote', 0, 3, 'https://apply', 0)")
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        db.query("SELECT * FROM internships").use { cursor ->
            assert(cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate2to3PreservesApplicationData() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO applications (id, internship_id, status, applied_at, last_updated, notes) VALUES ('1', 'int_1', 'APPLIED', 1000, 1000, 'test')")
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)
        db.query("SELECT * FROM applications").use { cursor ->
            assert(cursor.count > 0)
        }
    }
}
