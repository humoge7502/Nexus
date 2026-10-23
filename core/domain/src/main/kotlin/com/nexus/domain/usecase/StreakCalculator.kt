package com.nexus.domain.usecase

import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val isActive: Boolean,
)

class StreakCalculator @Inject constructor(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun calculateStreak(activeDates: List<LocalDate>): StreakData {
        if (activeDates.isEmpty()) return StreakData(0, 0, false)
        val today = LocalDate.now(clock)
        val sortedDates = activeDates.sortedDescending()
        val latestDate = sortedDates.first()
        val daysSinceLatest = ChronoUnit.DAYS.between(latestDate, today)
        if (daysSinceLatest > 1) {
            return StreakData(
                currentStreak = 0,
                longestStreak = calculateLongestStreak(sortedDates),
                isActive = false,
            )
        }
        var currentStreak = 1
        for (i in 0 until sortedDates.size - 1) {
            val daysBetween = ChronoUnit.DAYS.between(sortedDates[i + 1], sortedDates[i])
            if (daysBetween == 1L) currentStreak++
            else break
        }
        return StreakData(
            currentStreak = currentStreak,
            longestStreak = maxOf(currentStreak, calculateLongestStreak(sortedDates)),
            isActive = true,
        )
    }

    private fun calculateLongestStreak(dates: List<LocalDate>): Int {
        var longest = 1
        var current = 1
        for (i in 0 until dates.size - 1) {
            val daysBetween = ChronoUnit.DAYS.between(dates[i + 1], dates[i])
            if (daysBetween == 1L) { current++; longest = maxOf(longest, current) }
            else current = 1
        }
        return longest
    }
}
