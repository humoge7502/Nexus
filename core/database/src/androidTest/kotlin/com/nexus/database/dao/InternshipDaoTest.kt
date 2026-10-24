package com.nexus.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.database.NexusDatabase
import com.nexus.database.entity.InternshipEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InternshipDaoTest {

    private lateinit var db: NexusDatabase
    private lateinit var dao: InternshipDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NexusDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.internshipDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReadInternships() = runTest {
        val internship = InternshipEntity(
            id = "1", title = "Android Developer", companyName = "Google",
            location = "Mountain View", isRemote = false, durationMonths = 6,
            applyUrl = "https://google.com/careers",
        )
        dao.insert(internship)

        val result = dao.getAllInternships().first()
        assert(result.size == 1)
        assert(result[0].id == "1")
        assert(result[0].title == "Android Developer")
        assert(result[0].companyName == "Google")
    }

    @Test
    fun filterByRemoteInternships() = runTest {
        val remote1 = InternshipEntity(
            id = "1", title = "Remote Dev", companyName = "Co1",
            location = "Remote", isRemote = true, durationMonths = 3,
            applyUrl = "https://co1.com",
        )
        val remote2 = InternshipEntity(
            id = "2", title = "Remote Backend", companyName = "Co2",
            location = "Remote", isRemote = true, durationMonths = 6,
            applyUrl = "https://co2.com",
        )
        val onsite = InternshipEntity(
            id = "3", title = "Onsite Dev", companyName = "Co3",
            location = "NYC", isRemote = false, durationMonths = 3,
            applyUrl = "https://co3.com",
        )
        dao.insertAll(listOf(remote1, remote2, onsite))

        val remoteResults = dao.getFilteredInternships(isRemote = true).first()
        assert(remoteResults.size == 2)
        assert(remoteResults.all { it.isRemote })

        val onsiteResults = dao.getFilteredInternships(isRemote = false).first()
        assert(onsiteResults.size == 1)
        assert(!onsiteResults[0].isRemote)
    }

    @Test
    fun updateSaveStatus() = runTest {
        val internship = InternshipEntity(
            id = "1", title = "Test Intern", companyName = "TestCo",
            location = "Remote", isRemote = true, durationMonths = 3,
            applyUrl = "https://test.com",
        )
        dao.insert(internship)
        assert(!dao.getInternshipById("1")!!.isSaved)

        dao.updateSaveStatus("1", true)
        assert(dao.getInternshipById("1")!!.isSaved)

        dao.updateSaveStatus("1", false)
        assert(!dao.getInternshipById("1")!!.isSaved)
    }

    @Test
    fun deleteAllClearsTable() = runTest {
        val internship = InternshipEntity(
            id = "1", title = "Temp", companyName = "Co",
            location = "Remote", isRemote = false, durationMonths = 3,
            applyUrl = "https://co.com",
        )
        dao.insert(internship)
        assert(dao.getCount().first() == 1)

        dao.deleteAll()
        assert(dao.getCount().first() == 0)
    }
}
