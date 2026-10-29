package com.nexus.domain.usecase

import com.nexus.common.ApiResult
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.DSAProblem
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.Skill
import com.nexus.domain.model.StreakData
import com.nexus.domain.repository.IApplicationRepository
import com.nexus.domain.repository.IDSAProblemRepository
import com.nexus.domain.repository.ISkillGraphRepository
import com.nexus.domain.repository.IStreakRepository
import com.nexus.domain.repository.IUserXpRepository
import com.nexus.domain.repository.XPAwardResult
import io.mockk.mockk
import java.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementProbabilityEngineTest {

    private class FakeDsaRepository(
        private val solvedCount: Int,
        private val problems: List<DSAProblem>,
    ) : IDSAProblemRepository {
        override fun getAllProblems(): Flow<List<DSAProblem>> = flowOf(problems)
        override fun getByDifficulty(difficulty: String): Flow<List<DSAProblem>> = flowOf(emptyList())
        override suspend fun getDailyChallenge(): DSAProblem? = null
        override suspend fun updateStatus(id: String, status: DsaStatus) = Unit
        override suspend fun getSolvedCount(): Int = solvedCount
        override suspend fun refreshProblems(): ApiResult<List<DSAProblem>> = ApiResult.Success(problems)
    }

    private class FakeSkillRepository(private val skills: List<Skill>) : ISkillGraphRepository {
        override suspend fun initializeIfEmpty() = Unit
        override suspend fun markKnownSkill(skillId: String) = Unit
        override suspend fun getRecommendedSkills(): List<Skill> = skills
        override suspend fun getUserSkills(): List<Skill> = skills
    }

    private class FakeStreakRepository(private val streak: Int) : IStreakRepository {
        override val currentStreak: Flow<StreakData> =
            flowOf(StreakData(currentStreak = streak, longestStreak = streak, isActive = streak > 0))
        override suspend fun checkAndUpdateStreaks() = Unit
    }

    private class FakeXpRepository(private val xp: Int) : IUserXpRepository {
        override val totalXP: Flow<Int> = flowOf(xp)
        override val currentLevel: Flow<Int> = flowOf(1)
        override suspend fun awardXP(amount: Int, source: String) =
            XPAwardResult(amount, xp + amount, false, 1, source)
        override fun getSnapshotsForUser(): Flow<List<com.nexus.domain.model.ScoreSnapshot>> = flowOf(emptyList())
    }

    private class FakeApplicationRepository : IApplicationRepository {
        override fun getAllApplications(): Flow<List<Application>> = flowOf(emptyList())
        override fun getByStatus(status: ApplicationStatus): Flow<List<Application>> = flowOf(emptyList())
        override fun getApplicationCounts() = flowOf(com.nexus.domain.repository.ApplicationCounts())
        override suspend fun trackApplication(
            opportunityId: String,
            company: String,
            role: String,
            type: String,
            matchScore: Float,
        ) = Unit
        override suspend fun updateStatus(id: String, status: ApplicationStatus) = Unit
        override suspend fun delete(id: String) = Unit
        override suspend fun refreshApplications(): ApiResult<Unit> = ApiResult.Success(Unit)
    }

    private fun problem(topic: String): DSAProblem = DSAProblem(
        id = "p-$topic-${counter++}",
        title = "Problem $topic",
        difficulty = DsaDifficulty.MEDIUM,
        topics = persistentListOf(topic),
        status = DsaStatus.SOLVED,
        solvedAt = Instant.parse("2026-10-01T00:00:00Z"),
    )

    private var counter = 0

    private fun engine(
        solvedCount: Int,
        problems: List<DSAProblem> = emptyList(),
        skills: Int = 0,
        streak: Int = 0,
        xp: Int = 0,
    ) = PlacementProbabilityEngine(
        dsaRepo = FakeDsaRepository(solvedCount, problems),
        skillRepo = FakeSkillRepository((1..skills).map { Skill(id = "s$it", name = "Skill $it") }),
        appRepo = FakeApplicationRepository(),
        streakRepo = FakeStreakRepository(streak),
        xpRepo = FakeXpRepository(xp),
        userPrefs = mockk<UserPreferences>(relaxed = true),
    )

    @Test
    fun `empty profile yields floor probabilities and a dsa bottleneck`() = runTest {
        val result = engine(solvedCount = 0).calculate()

        assertEquals(2, result.tier1.probability)
        assertEquals(5, result.tier2.probability)
        assertEquals(10, result.tier3.probability)
        assertEquals("DSA volume — solve 50 more problems first", result.primaryBottleneck)
        assertTrue(result.tier1.keyRequirements.first().contains("you have: 0"))
        assertTrue(result.tier1.topImprovingActions.contains("Solve 150 more DSA problems"))
    }

    @Test
    fun `strong profile reaches tier caps and system design bottleneck`() = runTest {
        val result = engine(
            solvedCount = 200,
            problems = (1..15).map { problem("graphs") } + (1..15).map { problem("dynamic_programming") },
            skills = 10,
            streak = 60,
            xp = 20_000,
        ).calculate()

        assertEquals(85, result.tier1.probability)
        assertEquals(90, result.tier2.probability)
        assertEquals(95, result.tier3.probability)
        assertEquals("System Design — the next level for Tier 1", result.primaryBottleneck)
        assertTrue(result.tier1.topImprovingActions.isEmpty())
        assertTrue(result.tier2.topImprovingActions.isEmpty())
    }

    @Test
    fun `thin graphs are the first bottleneck`() = runTest {
        val result = engine(solvedCount = 60, problems = listOf(problem("arrays")), skills = 5, streak = 20)
            .calculate()

        assertEquals("Graph problems — critical for product companies", result.primaryBottleneck)
        assertEquals(listOf("Solve 90 more DSA problems", "Practice 10 graph problems"), result.tier1.topImprovingActions)
    }

    @Test
    fun `dynamic programming is the second bottleneck`() = runTest {
        val result = engine(
            solvedCount = 60,
            problems = (1..5).map { problem("graphs") } + listOf(problem("dynamic_programming")),
            skills = 5,
            streak = 20,
        ).calculate()

        assertEquals("Dynamic programming — required for Tier 1 interviews", result.primaryBottleneck)
    }

    @Test
    fun `consistency bottleneck appears after topic coverage`() = runTest {
        val result = engine(
            solvedCount = 60,
            problems = (1..5).map { problem("graphs") } + (1..5).map { problem("dynamic_programming") },
            skills = 5,
            streak = 10,
        ).calculate()

        assertEquals("Consistency — build a 14-day streak", result.primaryBottleneck)
    }

    @Test
    fun `skill diversity bottleneck appears after consistency`() = runTest {
        val result = engine(
            solvedCount = 60,
            problems = (1..5).map { problem("graphs") } + (1..5).map { problem("dynamic_programming") },
            skills = 3,
            streak = 20,
        ).calculate()

        assertEquals("Skill diversity — add 2 more skills", result.primaryBottleneck)
    }
}
