package com.nexus.data.usecase

import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.data.repository.ApplicationRepository
import com.nexus.database.dao.SkillGraphDao
import com.nexus.database.entity.SkillNodeEntity
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementReadinessCalculator @Inject constructor(
private val userXpRepository: UserXpRepository,
private val skillGraphDao: SkillGraphDao,
private val applicationRepository: ApplicationRepository,
private val dsaProblemRepository: DSAProblemRepository,
private val streakRepository: StreakRepository,
private val userPreferences: UserPreferences,
) {

data class PlacementReadiness(
val score: Int,
val level: String,
val dsaScore: Float,
val skillScore: Float,
val consistencyScore: Float,
val applicationScore: Float,
val engagementScore: Float,
val actionItems: List<PlacementAction>,
val estimatedPlacementMonth: String,
)

data class PlacementAction(
val priority: Int,
val title: String,
val description: String,
val impact: String,
val category: String,
)

suspend fun calculate(): PlacementReadiness {
val totalXP = userXpRepository.totalXP.first()
val dsaSolved = dsaProblemRepository.getSolvedCount()
val streak = streakRepository.currentStreak.first().currentStreak
val applicationCounts = applicationRepository.getApplicationCounts().first()

val dsaScore = when {
dsaSolved >= 200 -> 25f
dsaSolved >= 150 -> 22f
dsaSolved >= 100 -> 18f
dsaSolved >= 50 -> 12f
dsaSolved >= 25 -> 7f
else -> (dsaSolved.toFloat() / 25) * 7
}

val userSkillCount = skillGraphDao.getUserSkillCount().first()
val skillScore = when {
userSkillCount >= 12 -> 20f
userSkillCount >= 8 -> 16f
userSkillCount >= 5 -> 12f
userSkillCount >= 3 -> 8f
else -> (userSkillCount.toFloat() / 3) * 8
}

val consistencyScore = when {
streak >= 60 -> 20f
streak >= 30 -> 17f
streak >= 14 -> 13f
streak >= 7 -> 9f
streak >= 3 -> 6f
else -> (streak.toFloat() / 3) * 6
}

val applicationScore = when {
applicationCounts.offered >= 1 -> 20f
applicationCounts.interviewing >= 3 -> 17f
applicationCounts.interviewing >= 1 -> 14f
applicationCounts.total >= 10 -> 10f
applicationCounts.total >= 5 -> 7f
applicationCounts.total >= 1 -> 4f
else -> 0f
}

val engagementScore = when {
totalXP >= 5000 -> 15f
totalXP >= 2000 -> 12f
totalXP >= 1000 -> 9f
totalXP >= 500 -> 6f
totalXP >= 100 -> 3f
else -> 0f
}

val totalScore = (dsaScore + skillScore + consistencyScore + applicationScore + engagementScore).toInt().coerceIn(0, 100)

val actionItems = mutableListOf<PlacementAction>()
if (dsaScore < 12f) {
actionItems.add(PlacementAction(priority = 1, title = "Solve more DSA problems", description = "You need ${maxOf(0, 50 - dsaSolved)} more problems for basic placement readiness", impact = "HIGH", category = "DSA"))
}
if (skillScore < 12f) {
actionItems.add(PlacementAction(priority = 2, title = "Add more skills", description = "Add ${maxOf(0, 5 - userSkillCount)} more skills to your profile", impact = "MEDIUM", category = "SKILLS"))
}
if (consistencyScore < 9f) {
actionItems.add(PlacementAction(priority = 3, title = "Build a daily streak", description = "A 7-day streak significantly increases your readiness score", impact = "HIGH", category = "CONSISTENCY"))
}
if (applicationScore < 10f) {
actionItems.add(PlacementAction(priority = 4, title = "Apply to more opportunities", description = "Track at least 5 applications to show active job search", impact = "MEDIUM", category = "APPLICATIONS"))
}

val collegeTier = userPreferences.collegeTier.first()

return PlacementReadiness(
score = totalScore,
level = readinessLevel(totalScore.toFloat()),
dsaScore = dsaScore,
skillScore = skillScore,
consistencyScore = consistencyScore,
applicationScore = applicationScore,
engagementScore = engagementScore,
actionItems = actionItems.sortedBy { it.priority },
estimatedPlacementMonth = estimatePlacementMonth(totalScore.toFloat(), collegeTier),
)
}

private fun readinessLevel(score: Float): String = when {
score >= 80 -> "Placement Ready"
score >= 60 -> "Nearly Ready"
score >= 40 -> "Building Up"
score >= 20 -> "Early Stage"
else -> "Getting Started"
}

private fun estimatePlacementMonth(score: Float, collegeTier: String): String {
val tierAdjustment = when (collegeTier.uppercase()) {
"TIER_1", "IIT", "BITS" -> -1
"TIER_3", "STATE" -> 1
else -> 0
}
val baseMonths = when {
score >= 80 -> 1
score >= 60 -> 2
score >= 40 -> 4
else -> 6
}
val total = (baseMonths + tierAdjustment).coerceAtLeast(1)
val target = java.time.LocalDate.now().plusMonths(total.toLong())
return target.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + target.year
}
}
