package com.nexus.data.usecase

import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.domain.repository.ApplicationCounts
import com.nexus.database.dao.GoalDao
import com.nexus.domain.model.InsightCategory
import com.nexus.domain.model.InsightPriority
import com.nexus.domain.model.ProactiveInsight
import com.nexus.domain.model.StreakData
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ProactiveInsightEngineTest {

  private lateinit var streakRepository: StreakRepository
  private lateinit var applicationRepository: ApplicationRepository
  private lateinit var goalDao: GoalDao
  private lateinit var careerMemoryManager: CareerMemoryManager
  private lateinit var engine: ProactiveInsightEngine

  @Before
  fun setUp() {
    streakRepository = mockk(relaxed = true)
    applicationRepository = mockk(relaxed = true)
    goalDao = mockk(relaxed = true)
    careerMemoryManager = mockk(relaxed = true)
    engine = ProactiveInsightEngine(
      streakRepository,
      applicationRepository,
      goalDao,
      careerMemoryManager,
      placementReadinessCalculator = mockk(relaxed = true),
    )
  }

  @Test
  fun `generateDailyInsight with zero streak returns insight`() = runTest {
    every { streakRepository.currentStreak } returns flowOf(
      StreakData(currentStreak = 0, longestStreak = 0, isActive = false),
    )
    every { applicationRepository.getApplicationCounts() } returns flowOf(
      ApplicationCounts(applied = 0, interviewing = 0, offered = 0, accepted = 0, total = 0),
    )
    every { goalDao.getActive() } returns flowOf(emptyList())

    val insight = engine.generateDailyInsight()
    assertNotNull(insight)
    assertNotNull(insight!!.title)
    assertNotNull(insight.category)
    assertNotNull(insight.priority)
  }

  @Test
  fun `generateDailyInsight with active streak returns streak insight`() = runTest {
    every { streakRepository.currentStreak } returns flowOf(
      StreakData(currentStreak = 7, longestStreak = 7, isActive = true),
    )
    every { applicationRepository.getApplicationCounts() } returns flowOf(
      ApplicationCounts(applied = 3, interviewing = 1, offered = 0, accepted = 0, total = 3),
    )
    every { goalDao.getActive() } returns flowOf(emptyList())

    val insight = engine.generateDailyInsight()
    assertNotNull(insight)
    assertTrue(insight!!.title.contains("Streak"))
  }

  @Test
  fun `generateDailyInsight returns category from valid set`() = runTest {
    every { streakRepository.currentStreak } returns flowOf(
      StreakData(currentStreak = 0, longestStreak = 0, isActive = false),
    )
    every { applicationRepository.getApplicationCounts() } returns flowOf(
      ApplicationCounts(0, 0, 0, 0, 0),
    )
    every { goalDao.getActive() } returns flowOf(emptyList())

    val insight = engine.generateDailyInsight()
    assertNotNull(insight)
    assertTrue(insight!!.category in listOf(InsightCategory.GROWTH, InsightCategory.APPLICATION, InsightCategory.GOAL, InsightCategory.LEARNING))
  }

  @Test
  fun `generateDailyInsight returns priority from valid set`() = runTest {
    every { streakRepository.currentStreak } returns flowOf(
      StreakData(0, 0, false),
    )
    every { applicationRepository.getApplicationCounts() } returns flowOf(
      ApplicationCounts(0, 0, 0, 0, 0),
    )
    every { goalDao.getActive() } returns flowOf(emptyList())

    val insight = engine.generateDailyInsight()
    assertNotNull(insight)
    assertTrue(insight!!.priority in listOf(InsightPriority.CRITICAL, InsightPriority.HIGH, InsightPriority.NORMAL, InsightPriority.LOW))
  }
}
