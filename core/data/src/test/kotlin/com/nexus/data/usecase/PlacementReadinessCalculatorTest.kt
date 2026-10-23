package com.nexus.data.usecase

import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.data.repository.ApplicationRepository
import com.nexus.domain.repository.ApplicationCounts
import com.nexus.database.dao.SkillGraphDao
import com.nexus.domain.usecase.StreakData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlacementReadinessCalculatorTest {
  private lateinit var mockXpRepo: UserXpRepository
  private lateinit var mockSkillDao: SkillGraphDao
  private lateinit var mockAppRepo: ApplicationRepository
  private lateinit var mockDsaRepo: DSAProblemRepository
  private lateinit var mockStreakRepo: StreakRepository
  private lateinit var calculator: PlacementReadinessCalculator

  @Before
  fun setup() {
    mockXpRepo = mockk(relaxed = true)
    mockSkillDao = mockk(relaxed = true)
    mockAppRepo = mockk(relaxed = true)
    mockDsaRepo = mockk(relaxed = true)
    mockStreakRepo = mockk(relaxed = true)
    calculator = PlacementReadinessCalculator(
      mockXpRepo, mockSkillDao, mockAppRepo, mockDsaRepo,
      mockStreakRepo, mockk(relaxed = true),
    )
  }

  private fun setupMocks(
    xp: Int, dsa: Int, skills: Int, streak: Int,
    appsTotal: Int, appsOffered: Int, appsInterviewing: Int,
  ) {
    every { mockXpRepo.totalXP } returns flowOf(xp)
    every { mockDsaRepo.getSolvedCount() } returns dsa
    every { mockStreakRepo.currentStreak } returns flowOf(
      StreakData(currentStreak = streak, longestStreak = streak, isActive = true))
    every { mockAppRepo.getApplicationCounts() } returns flowOf(
      ApplicationCounts(
        applied = 0, interviewing = appsInterviewing,
        offered = appsOffered, accepted = 0, total = appsTotal,
      ),
    )
    every { mockSkillDao.getUserSkillCount() } returns flowOf(skills)
  }

  @Test
  fun `all zero inputs returns score under 10`() = runTest {
    setupMocks(0, 0, 0, 0, 0, 0, 0)
    assertTrue(calculator.calculate().score < 10)
  }

  @Test
  fun `placement ready user scores above 80`() = runTest {
    setupMocks(5000, 200, 12, 60, 5, 1, 3)
    assertTrue(calculator.calculate().score >= 80)
  }

  @Test
  fun `score stays within 0-100 bounds`() = runTest {
    setupMocks(999999, 999, 50, 365, 100, 5, 10)
    assertTrue(calculator.calculate().score in 0..100)
  }

  @Test
  fun `50 DSA problems meets minimum DSA score`() = runTest {
    setupMocks(0, 50, 0, 0, 0, 0, 0)
    assertTrue(calculator.calculate().dsaScore >= 12f)
  }

  @Test
  fun `low score user has non-empty action items`() = runTest {
    setupMocks(50, 5, 1, 0, 0, 0, 0)
    assertTrue(calculator.calculate().actionItems.isNotEmpty())
  }

  @Test
  fun `action items sorted by ascending priority`() = runTest {
    setupMocks(50, 5, 1, 0, 0, 0, 0)
    val priorities = calculator.calculate().actionItems.map { it.priority }
    assertEquals(priorities, priorities.sorted())
  }

  @Test
  fun `offer received gives max application score`() = runTest {
    setupMocks(0, 0, 0, 0, 1, 1, 0)
    assertEquals(20f, calculator.calculate().applicationScore, 0.01f)
  }

  @Test
  fun `high score level is Placement Ready`() = runTest {
    setupMocks(5000, 200, 12, 60, 5, 1, 3)
    assertEquals("Placement Ready", calculator.calculate().level)
  }

  @Test
  fun `estimated placement month is non-blank`() = runTest {
    setupMocks(0, 0, 0, 0, 0, 0, 0)
    assertTrue(calculator.calculate().estimatedPlacementMonth.isNotBlank())
  }
}
