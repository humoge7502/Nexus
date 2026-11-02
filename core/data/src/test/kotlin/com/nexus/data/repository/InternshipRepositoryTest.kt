package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.InternshipEntity
import com.nexus.domain.model.OpportunityKind
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class InternshipRepositoryTest {

  private lateinit var feed: OpportunityFeedRepository

  @MockK
  private lateinit var dao: InternshipDao

  private lateinit var repository: InternshipRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    feed = mockk()
    repository = InternshipRepository(feed, dao)
  }

  private fun entity(
    id: String = "1",
    deadline: Long = 0L,
  ) = InternshipEntity(
    id = id,
    title = "Test Intern",
    companyName = "TestCo",
    location = "Remote",
    isRemote = true,
    durationMonths = 3,
    applyUrl = "https://test.com/apply",
    deadline = deadline,
    kind = "PROGRAM",
    sourceName = "Greenhouse",
    verifiedAt = 1_700_000_000_000L,
    stipendLabel = "Paid stipend",
    durationLabel = "12 weeks",
    tags = "Remote, Scholarship",
    skills = "Kotlin, Android",
  )

  @Test
  fun `getAllInternships maps feed fields and treats zero deadline as rolling`() = runTest {
    every { dao.getAllInternships() } returns flowOf(listOf(entity()))

    val internship = repository.getAllInternships().first().single()

    assertEquals("Test Intern", internship.title)
    assertEquals("TestCo", internship.company.name)
    assertNull(internship.deadline)
    assertEquals(OpportunityKind.PROGRAM, internship.kind)
    assertEquals("Greenhouse", internship.sourceName)
    assertEquals("Paid stipend", internship.stipendLabel)
    assertEquals("12 weeks", internship.durationLabel)
    assertEquals(listOf("Remote", "Scholarship"), internship.tags)
    assertEquals(listOf("Kotlin", "Android"), internship.requiredSkills.map { it.name })
    assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), internship.verifiedAt)
  }

  @Test
  fun `getAllInternships maps epoch-day deadline to a date`() = runTest {
    val day = LocalDate.of(2026, 12, 1).toEpochDay()
    every { dao.getAllInternships() } returns flowOf(listOf(entity(deadline = day)))

    val internship = repository.getAllInternships().first().single()

    assertEquals(LocalDate.of(2026, 12, 1), internship.deadline)
  }

  @Test
  fun `refreshInternships publishes feed rows`() = runTest {
    every { dao.getAllInternships() } returns flowOf(listOf(entity()))
    coEvery { feed.refresh() } returns ApiResult.Success(FeedSnapshot(1, 0, "2026-11-01T00:00:00Z"))

    val result = repository.refreshInternships()

    assertTrue(result.isSuccess)
    assertEquals(1, result.dataOrNull?.size)
    coVerify { feed.refresh() }
  }

  @Test
  fun `refreshInternships keeps cache when the network fails`() = runTest {
    every { dao.getAllInternships() } returns flowOf(listOf(entity()))
    coEvery { feed.refresh() } returns ApiResult.Error("Network error")

    val result = repository.refreshInternships()

    assertTrue(result.isSuccess)
    assertEquals("1", result.dataOrNull?.single()?.id)
  }

  @Test
  fun `refreshInternships returns error when empty and offline`() = runTest {
    every { dao.getAllInternships() } returns flowOf(emptyList())
    coEvery { feed.refresh() } returns ApiResult.Error("Network error")

    val result = repository.refreshInternships()

    assertTrue(result.isError)
    assertEquals("Network error", result.errorMessage)
  }
}
