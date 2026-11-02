package com.nexus.data.repository

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.ApplicationDao
import com.nexus.database.entity.ApplicationEntity
import com.nexus.domain.model.ApplicationStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ApplicationRepositoryTest {

    @MockK
    private lateinit var dao: ApplicationDao

    @MockK
    private lateinit var analyticsTracker: AnalyticsTracker

    private lateinit var repository: ApplicationRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        // AnalyticsTracker has two track() overloads, so the matchers need types.
        every { analyticsTracker.track(any<String>(), any<Map<String, Any>>()) } returns Unit
        repository = ApplicationRepository(dao, analyticsTracker)
    }

    private fun entity(
        id: String = "row-1",
        opportunityId: String = "opp-1",
        matchScore: Float = -1f,
    ) = ApplicationEntity(
        id = id,
        opportunityId = opportunityId,
        company = "TestCo",
        role = "Backend Intern",
        opportunityType = "INTERNSHIP",
        matchScore = matchScore,
    )

    @Test
    fun `a status change resolves the row by its own id`() = runTest {
        val updated = slot<ApplicationEntity>()
        coEvery { dao.getById("row-1") } returns entity()
        coEvery { dao.update(capture(updated)) } returns Unit

        repository.updateStatus("row-1", ApplicationStatus.APPLIED)

        assertEquals(ApplicationStatus.APPLIED.name, updated.captured.status)
        assertEquals("row-1", updated.captured.id)
        // The screen hands over the primary key; resolving it as an opportunity id
        // made every status change a silent no-op.
        coVerify(exactly = 0) { dao.getByOpportunityId(any()) }
    }

    @Test
    fun `a real zero score survives while the marker reads back as no score`() = runTest {
        every { dao.getAllApplications() } returns flowOf(
            listOf(
                entity(id = "scored-zero", matchScore = 0f),
                entity(id = "unscored", matchScore = -1f),
            )
        )

        val applications = repository.getAllApplications().first()

        assertEquals(0f, applications.first { it.id == "scored-zero" }.matchScore!!, 0f)
        assertNull(applications.first { it.id == "unscored" }.matchScore)
    }

    @Test
    fun `tracking an unscored role reports no score instead of a zero`() = runTest {
        val inserted = slot<ApplicationEntity>()
        val properties = slot<Map<String, Any>>()
        coEvery { dao.getByOpportunityId("opp-9") } returns null
        coEvery { dao.insert(capture(inserted)) } returns Unit
        every { analyticsTracker.track("application_tracked", capture(properties)) } returns Unit

        repository.trackApplication("opp-9", "TestCo", "Backend Intern", "INTERNSHIP", null)

        assertEquals(-1f, inserted.captured.matchScore, 0f)
        assertFalse(properties.captured.containsKey("match_score"))
    }
}
