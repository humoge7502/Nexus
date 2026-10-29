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
        helper.runMigrationsAndValidate(TEST_DB, 10, true,
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
            MIGRATION_8_9, MIGRATION_9_10
        )
    }

    @Test
    @Throws(IOException::class)
    fun migrate1to2PreservesData() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO internships (id, title, company_name, location, is_remote, stipend_currency, duration_months, skills, deadline, apply_url, description, posted_at, is_saved, cached_at) VALUES ('1', 'Test', 'Test Corp', 'Remote', 0, 'INR', 3, '[]', 0, 'https://apply', 'desc', 0, 0, 0)")
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        db.query("SELECT * FROM internships").use { cursor ->
            assert(cursor.count > 0)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate8to9PreservesInternships() {
        helper.createDatabase(TEST_DB, 8).apply {
            execSQL("INSERT INTO internships (id, title, company_name, location, is_remote, stipend_currency, duration_months, skills, deadline, apply_url, description, posted_at, is_saved, cached_at) VALUES ('1', 'Test', 'Test Corp', 'Remote', 0, 'INR', 3, '[]', 0, 'https://apply', 'desc', 0, 0, 0)")
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        db.query("SELECT * FROM internships").use { cursor ->
            assert(cursor.count > 0)
        }
        db.query("PRAGMA index_list('internships')").use { cursor ->
            val names = buildList {
                while (cursor.moveToNext()) add(cursor.getString(1))
            }
            assert(names.contains("index_internships_posted_at"))
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

    @Test
    @Throws(IOException::class)
    fun migrate9to10AddsCertificationProgressAndPreservesData() {
        helper.createDatabase(TEST_DB, 9).apply {
            execSQL("INSERT INTO internships (id, title, company_name, location, is_remote, stipend_currency, duration_months, skills, deadline, apply_url, description, posted_at, is_saved, cached_at) VALUES ('1', 'Test', 'Test Corp', 'Remote', 0, 'INR', 3, '[]', 0, 'https://apply', 'desc', 0, 0, 0)")
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)
        db.query("SELECT * FROM internships").use { cursor ->
            assert(cursor.count > 0)
        }
        db.query("SELECT * FROM certification_progress").use { cursor ->
            assert(cursor.count == 0)
        }
        db.execSQL("INSERT INTO certification_progress (certification_id, completed_modules, total_modules, study_hours_logged) VALUES ('cert_1', 2, 8, 5)")
        db.query("SELECT completed_modules, total_modules FROM certification_progress WHERE certification_id = 'cert_1'").use { cursor ->
            assert(cursor.moveToFirst())
            assert(cursor.getInt(0) == 2)
            assert(cursor.getInt(1) == 8)
        }
    }
}
