package com.nexus.data.usecase

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.RetentionDao
import com.nexus.database.entity.RetentionEntity
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetentionTrackerTest {

    private val retentionDao: RetentionDao = mockk()
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val xpRepository: UserXpRepository = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private lateinit var tracker: RetentionTracker

    @Before
    fun setUp() {
        coEvery { userPreferences.lastLoginDate } returns flowOf("")
        coEvery { xpRepository.totalXP } returns flowOf(0)
        tracker = RetentionTracker(retentionDao, userPreferences, xpRepository, analyticsTracker)
    }

    @Test
    fun `recordWeeklyActivity inserts entity`() = runTest {
        coEvery { retentionDao.insert(any()) } returns Unit

        tracker.recordWeeklyActivity(sessionCount = 5, featuresUsed = listOf("home", "explore"))

        coVerify(exactly = 1) { retentionDao.insert(any<RetentionEntity>()) }
    }

    @Test
    fun `recordWeeklyActivity stores correct session count`() = runTest {
        var captured: RetentionEntity? = null
        coEvery { retentionDao.insert(any()) } answers {
            captured = firstArg()
            Unit
        }

        tracker.recordWeeklyActivity(sessionCount = 3, featuresUsed = listOf("ai"))

        assertTrue(captured != null)
        assertEquals(3, captured?.sessionCount)
    }

    @Test
    fun `recordWeeklyActivity stores features as comma separated string`() = runTest {
        var captured: RetentionEntity? = null
        coEvery { retentionDao.insert(any()) } answers {
            captured = firstArg()
            Unit
        }

        tracker.recordWeeklyActivity(sessionCount = 1, featuresUsed = listOf("home", "explore", "ai"))

        assertTrue(captured != null)
        assertEquals("home,explore,ai", captured?.featuresUsed)
    }

    @Test
    fun `getRetentionTrajectory returns empty list when no install week`() = runTest {
        coEvery { retentionDao.getInstallWeek() } returns null

        val result = tracker.getRetentionTrajectory()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRetentionTrajectory returns cohort data for install week`() = runTest {
        val events = listOf(
            RetentionEntity(
                installWeek = "2026-W18", activityWeek = "2026-W18",
                weeksSinceInstall = 0, sessionCount = 5, xpEarned = 100,
                featuresUsed = "home"
            ),
            RetentionEntity(
                installWeek = "2026-W18", activityWeek = "2026-W19",
                weeksSinceInstall = 1, sessionCount = 3, xpEarned = 50,
                featuresUsed = "explore"
            ),
        )
        coEvery { retentionDao.getInstallWeek() } returns "2026-W18"
        coEvery { retentionDao.getCohortData("2026-W18") } returns flowOf(events)

        val result = tracker.getRetentionTrajectory()

        assertEquals(2, result.size)
        assertEquals("2026-W18", result[0].installWeek)
        assertEquals(0, result[0].weeksSinceInstall)
        assertEquals(1, result[1].weeksSinceInstall)
    }

    @Test
    fun `recordWeeklyActivity tracks analytics event`() = runTest {
        coEvery { retentionDao.insert(any()) } returns Unit

        tracker.recordWeeklyActivity(sessionCount = 2, featuresUsed = listOf("home"))

        coVerify { analyticsTracker.track("weekly_activity_recorded", any()) }
    }
}
