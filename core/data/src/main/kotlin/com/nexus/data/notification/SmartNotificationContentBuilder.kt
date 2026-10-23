package com.nexus.data.notification

import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.CareerHitRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartNotificationContentBuilder @Inject constructor(
private val streakRepository: StreakRepository,
private val userXpRepository: UserXpRepository,
private val careerHitRepository: CareerHitRepository,
private val analyticsTracker: AnalyticsTracker,
) {

data class NotificationContent(val title: String, val body: String)

suspend fun buildMorningContent(): NotificationContent {
val streak = streakRepository.currentStreak.first().currentStreak
val todayHitCompleted = careerHitRepository.isHitViewedToday()
val totalXP = userXpRepository.totalXP.first()
val level = userXpRepository.levelName(userXpRepository.currentLevel.first())

val title = when {
streak == 0 -> "Start your streak today \uD83D\uDE80"
streak == 1 -> "Day 2 begins! Keep it going \uD83D\uDD25"
streak in 2..6 -> "Day ${streak + 1} — building momentum \uD83D\uDD25"
streak == 7 -> "One week streak! You're in the top 20% \uD83C\uDFAF"
streak in 8..29 -> "$streak days strong! Don't break the chain \uD83D\uDD25"
streak == 30 -> "30-DAY STREAK! Exceptional \uD83C\uDFC6"
else -> "$streak days! Legendary \uD83C\uDFC6"
}

val body = if (!todayHitCompleted) {
"Your daily career hit is ready. 2 minutes. ${Constants.XP_PER_DAILY_HIT} XP. Total: $totalXP XP as a $level."
} else {
"Great job today! Come back tomorrow to keep your streak alive."
}

return NotificationContent(title = title, body = body)
}

suspend fun buildEveningContent(): NotificationContent {
val todayXP = userXpRepository.xpEarnedToday.first()
val streak = streakRepository.currentStreak.first().currentStreak
val hitCompleted = careerHitRepository.isHitViewedToday()
val level = userXpRepository.levelName(userXpRepository.currentLevel.first())

val title = when {
!hitCompleted && streak > 7 -> "\u26A0\uFE0F $streak-day streak at risk!"
!hitCompleted -> "Your career hit is still waiting"
todayXP > 200 -> "Excellent day — $todayXP XP! \u2B50"
todayXP > 50 -> "Good progress today — $todayXP XP \uD83D\uDCC8"
else -> "Today's recap"
}

val body = when {
!hitCompleted && streak > 7 ->
"You have until midnight to save your $streak-day streak. Takes 2 minutes. Don't let it end tonight."
!hitCompleted ->
"Complete your daily hit before midnight to maintain your streak."
else ->
"You earned $todayXP XP today. See tomorrow's opportunities in the morning brief."
}

return NotificationContent(title = title, body = body)
}

suspend fun buildDeadlineContent(company: String, role: String, daysLeft: Int): NotificationContent {
return NotificationContent(
title = "\u23F0 $daysLeft day${if (daysLeft > 1) "s" else ""} left — $company",
body = "The $role deadline at $company is approaching. Tap to get an AI interview prep plan.",
)
}
}
