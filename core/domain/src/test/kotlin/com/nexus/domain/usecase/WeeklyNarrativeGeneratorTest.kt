package com.nexus.domain.usecase

import com.nexus.common.ApiResult
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.DSAProblem
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.model.StreakData
import com.nexus.domain.repository.IDSAProblemRepository
import com.nexus.domain.repository.IStreakRepository
import com.nexus.domain.repository.IUserXpRepository
import com.nexus.domain.repository.XPAwardResult
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyNarrativeGeneratorTest {

    private class FakeXpRepository(
        private val xp: Int,
        private val snapshots: List<Int>,
    ) : IUserXpRepository {
        override val totalXP: Flow<Int> = flowOf(xp)
        override val currentLevel: Flow<Int> = flowOf(1)
        override suspend fun awardXP(amount: Int, source: String) =
            XPAwardResult(amount, xp + amount, false, 1, source)
        override fun getSnapshotsForUser(): Flow<List<ScoreSnapshot>> =
            flowOf(snapshots.mapIndexed { index, score -> ScoreSnapshot(id = "s$index", score = score) })
    }

    private class FakeDsaRepository(private val solved: Int) : IDSAProblemRepository {
        private val problems = (1..solved).map {
            DSAProblem(
                id = "p$it",
                title = "Problem $it",
                difficulty = DsaDifficulty.EASY,
                status = DsaStatus.SOLVED,
                solvedAt = Instant.parse("2026-10-01T00:00:00Z"),
            )
        }
        override fun getAllProblems(): Flow<List<DSAProblem>> = flowOf(problems)
        override fun getByDifficulty(difficulty: String): Flow<List<DSAProblem>> = flowOf(emptyList())
        override suspend fun getDailyChallenge(): DSAProblem? = null
        override suspend fun updateStatus(id: String, status: DsaStatus) = Unit
        override suspend fun getSolvedCount(): Int = solved
        override suspend fun refreshProblems(): ApiResult<List<DSAProblem>> = ApiResult.Success(problems)
    }

    private class FakeStreakRepository(private val streak: Int) : IStreakRepository {
        override val currentStreak: Flow<StreakData> = flowOf(StreakData(currentStreak = streak))
        override suspend fun checkAndUpdateStreaks() = Unit
    }

    private class FailingXpRepository : IUserXpRepository {
        override val totalXP: Flow<Int> = flow { error("offline") }
        override val currentLevel: Flow<Int> = flow { error("offline") }
        override suspend fun awardXP(amount: Int, source: String) = error("offline")
        override fun getSnapshotsForUser(): Flow<List<ScoreSnapshot>> =
            flow { error("offline") }
    }

    private class FailingDsaRepository : IDSAProblemRepository {
        override fun getAllProblems(): Flow<List<DSAProblem>> = flow { error("offline") }
        override fun getByDifficulty(difficulty: String): Flow<List<DSAProblem>> =
            flow { error("offline") }
        override suspend fun getDailyChallenge(): DSAProblem? = error("offline")
        override suspend fun updateStatus(id: String, status: DsaStatus) = Unit
        override suspend fun getSolvedCount(): Int = error("offline")
        override suspend fun refreshProblems(): ApiResult<List<DSAProblem>> =
            error("offline")
    }

    private class FailingStreakRepository : IStreakRepository {
        override val currentStreak: Flow<StreakData> = flow { error("offline") }
        override suspend fun checkAndUpdateStreaks() = Unit
    }

    private fun generator(
        xp: IUserXpRepository = FakeXpRepository(0, emptyList()),
        dsa: IDSAProblemRepository = FakeDsaRepository(0),
        streak: IStreakRepository = FakeStreakRepository(0),
    ) = WeeklyNarrativeGenerator(
        xpRepository = xp,
        dsaRepository = dsa,
        streakRepository = streak,
        userPreferences = mockk<UserPreferences>(relaxed = true),
    )

    @Test
    fun `empty history produces a baseline narrative`() = runTest {
        val narrative = generator().generate()

        assertEquals("starting", narrative.trend)
        assertEquals("A new week of progress begins", narrative.headline)
        assertEquals("Start a 7-day streak this week", narrative.callToAction)
        assertEquals(0, narrative.weeklyXP)
        assertEquals(0, narrative.weeklyDSA)
        assertEquals(
            "This week: 0 XP, 0 DSA problems solved. This is your baseline.",
            narrative.body,
        )
    }

    @Test
    fun `accelerating week is celebrated`() = runTest {
        val narrative = generator(
            xp = FakeXpRepository(800, listOf(100, 300, 800)),
            dsa = FakeDsaRepository(5),
            streak = FakeStreakRepository(9),
        ).generate()

        assertEquals("accelerating", narrative.trend)
        assertEquals("Picking up speed", narrative.headline)
        assertEquals(
            "This week: 500 XP, 5 DSA problems solved. +300 XP more than last week. Seven days in — you're building a real habit.",
            narrative.body,
        )
        assertEquals("Maintain your 9-day streak — your most valuable asset", narrative.callToAction)
    }

    @Test
    fun `exceptional week gets the outstanding headline`() = runTest {
        val narrative = generator(
            xp = FakeXpRepository(1200, listOf(0, 100, 1200)),
            dsa = FakeDsaRepository(8),
            streak = FakeStreakRepository(30),
        ).generate()

        assertEquals("Outstanding week — real momentum", narrative.headline)
        assertEquals(1100, narrative.weeklyXP)
        assertTrue(narrative.body.contains("top 5% of NEXUS users"))
    }

    @Test
    fun `steady week keeps the rhythm`() = runTest {
        val narrative = generator(
            xp = FakeXpRepository(480, listOf(100, 300, 480)),
            dsa = FakeDsaRepository(2),
            streak = FakeStreakRepository(20),
        ).generate()

        assertEquals("steady", narrative.trend)
        assertEquals("Consistent — the most underrated quality", narrative.headline)
        assertEquals(
            "This week: 180 XP, 2 DSA problems solved. 20 XP less than last week. Your 20-day streak is genuinely impressive.",
            narrative.body,
        )
        assertEquals("Solve 3 DSA problems this week — minimum viable prep", narrative.callToAction)
    }

    @Test
    fun `declining week still leans on the streak`() = runTest {
        val narrative = generator(
            xp = FakeXpRepository(520, listOf(100, 500, 520)),
            dsa = FakeDsaRepository(4),
            streak = FakeStreakRepository(8),
        ).generate()

        assertEquals("declining", narrative.trend)
        assertEquals("Slower week — your streak says you'll bounce back", narrative.headline)
        assertTrue(narrative.body.endsWith("Seven days in — you're building a real habit."))
    }

    @Test
    fun `weekly xp falls back to total xp before two snapshots exist`() = runTest {
        val narrative = generator(
            xp = FakeXpRepository(700, listOf(300)),
            dsa = FakeDsaRepository(1),
            streak = FakeStreakRepository(3),
        ).generate()

        assertEquals(700, narrative.weeklyXP)
        assertEquals("starting", narrative.trend)
        assertTrue(narrative.body.startsWith("This week: 700 XP, 1 DSA problem solved."))
        assertTrue(narrative.body.endsWith("Day 3 — every streak starts here."))
    }

    @Test
    fun `offline repositories degrade gracefully`() = runTest {
        val narrative = generator(
            xp = FailingXpRepository(),
            dsa = FailingDsaRepository(),
            streak = FailingStreakRepository(),
        ).generate()

        assertEquals(0, narrative.weeklyXP)
        assertEquals(0, narrative.weeklyDSA)
        assertEquals("starting", narrative.trend)
        assertEquals("A new week of progress begins", narrative.headline)
    }
}
