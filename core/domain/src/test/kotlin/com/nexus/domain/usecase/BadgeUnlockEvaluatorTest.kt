package com.nexus.domain.usecase

import com.nexus.domain.model.AchievementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeUnlockEvaluatorTest {

    private val evaluator = BadgeUnlockEvaluator()

    private fun stats(
        streakDays: Int = 0,
        problemsSolved: Int = 0,
        certificationsCompleted: Int = 0,
        applicationsSubmitted: Int = 0,
        projectsCompleted: Int = 0,
        totalXP: Int = 0,
    ) = BadgeUnlockEvaluator.UserStats(
        streakDays = streakDays,
        problemsSolved = problemsSolved,
        certificationsCompleted = certificationsCompleted,
        applicationsSubmitted = applicationsSubmitted,
        projectsCompleted = projectsCompleted,
        totalXP = totalXP,
    )

    @Test
    fun `fresh user unlocks nothing`() {
        assertTrue(evaluator.evaluate(stats(), emptySet()).isEmpty())
    }

    @Test
    fun `streak badges unlock at their thresholds`() {
        assertEquals(listOf(AchievementType.STREAK_7), evaluator.evaluate(stats(streakDays = 7), emptySet()))
        assertEquals(
            listOf(AchievementType.STREAK_7, AchievementType.STREAK_30),
            evaluator.evaluate(stats(streakDays = 30), emptySet()),
        )
        assertTrue(evaluator.evaluate(stats(streakDays = 6), emptySet()).isEmpty())
    }

    @Test
    fun `first application and certification unlock immediately`() {
        assertEquals(
            listOf(AchievementType.FIRST_INTERNSHIP),
            evaluator.evaluate(stats(applicationsSubmitted = 1), emptySet()),
        )
        assertEquals(
            listOf(AchievementType.FIRST_CERTIFICATION),
            evaluator.evaluate(stats(certificationsCompleted = 1), emptySet()),
        )
    }

    @Test
    fun `volume badges require full thresholds`() {
        assertEquals(
            listOf(AchievementType.FIRST_INTERNSHIP, AchievementType.TEN_APPLICATIONS),
            evaluator.evaluate(stats(applicationsSubmitted = 10), emptySet()),
        )
        assertEquals(
            listOf(AchievementType.FIRST_CERTIFICATION, AchievementType.FIVE_CERTS),
            evaluator.evaluate(stats(certificationsCompleted = 5), emptySet()),
        )
        assertEquals(
            listOf(AchievementType.MASTER_CODER),
            evaluator.evaluate(stats(problemsSolved = 50), emptySet()),
        )
    }

    @Test
    fun `already unlocked badges are not returned again`() {
        val alreadyUnlocked = setOf(
            AchievementType.STREAK_7,
            AchievementType.FIRST_INTERNSHIP,
        )
        val result = evaluator.evaluate(
            stats(streakDays = 30, applicationsSubmitted = 10),
            alreadyUnlocked,
        )
        assertEquals(
            listOf(
                AchievementType.STREAK_30,
                AchievementType.TEN_APPLICATIONS,
            ),
            result,
        )
    }

    @Test
    fun `all badges unlock for a fully qualified user`() {
        val result = evaluator.evaluate(
            stats(
                streakDays = 30,
                problemsSolved = 50,
                certificationsCompleted = 5,
                applicationsSubmitted = 10,
                projectsCompleted = 4,
                totalXP = 10_000,
            ),
            emptySet(),
        )
        assertEquals(7, result.size)
        assertTrue(result.containsAll(AchievementType.entries.filter {
            it == AchievementType.STREAK_7 ||
                it == AchievementType.STREAK_30 ||
                it == AchievementType.FIRST_INTERNSHIP ||
                it == AchievementType.FIRST_CERTIFICATION ||
                it == AchievementType.TEN_APPLICATIONS ||
                it == AchievementType.FIVE_CERTS ||
                it == AchievementType.MASTER_CODER
        }))
    }
}
