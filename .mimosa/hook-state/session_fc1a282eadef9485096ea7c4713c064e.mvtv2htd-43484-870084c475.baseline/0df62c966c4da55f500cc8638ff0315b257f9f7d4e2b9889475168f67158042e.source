package com.nexus.domain.usecase

import com.nexus.domain.repository.IDSAProblemRepository
import com.nexus.domain.repository.ISkillGraphRepository
import com.nexus.domain.repository.IApplicationRepository
import com.nexus.domain.repository.IStreakRepository
import com.nexus.domain.repository.IUserXpRepository
import com.nexus.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementProbabilityEngine @Inject constructor(
  private val dsaRepo: IDSAProblemRepository,
  private val skillRepo: ISkillGraphRepository,
  private val appRepo: IApplicationRepository,
  private val streakRepo: IStreakRepository,
  private val xpRepo: IUserXpRepository,
  private val userPrefs: UserPreferences,
) {

  data class TierProbability(
    val tierName: String,
    val examples: String,
    val probability: Int,
    val keyRequirements: List<String>,
    val topImprovingActions: List<String>,
  )

  data class PlacementProbabilities(
    val tier1: TierProbability,
    val tier2: TierProbability,
    val tier3: TierProbability,
    val primaryBottleneck: String,
    val dataDisclaimer: String = "Estimates based on your preparation patterns. Accuracy improves as more NEXUS students share outcomes.",
  )

  suspend fun calculate(): PlacementProbabilities {
    val totalDSA = dsaRepo.getSolvedCount()
    val allProblems = dsaRepo.getAllProblems().first()
    val recentProblems = allProblems.filter { it.solvedAt != null }
    val topicDistribution = recentProblems.flatMap { it.topics }.groupBy { it }.mapValues { it.value.size }
    val skillCount = skillRepo.getRecommendedSkills().size
    val streak = streakRepo.currentStreak.first().currentStreak
    val totalXP = xpRepo.totalXP.first()
    val offers = appRepo.getByStatus(com.nexus.domain.model.ApplicationStatus.OFFERED).first().size
    val interviews = appRepo.getByStatus(com.nexus.domain.model.ApplicationStatus.INTERVIEWING).first().size

    val tier1 = calcTier1(totalDSA, topicDistribution, skillCount, streak, totalXP)
    val tier2 = calcTier2(totalDSA, skillCount, streak, totalXP)
    val tier3 = calcTier3(totalDSA, skillCount, totalXP)
    val bottleneck = identifyBottleneck(totalDSA, topicDistribution, skillCount, streak)

    return PlacementProbabilities(tier1, tier2, tier3, bottleneck)
  }

  private fun calcTier1(totalDSA: Int, topics: Map<String, Int>, skills: Int, streak: Int, xp: Int): TierProbability {
    val dsaScore = when { totalDSA >= 200 -> 1f; totalDSA >= 150 -> 0.85f; totalDSA >= 100 -> 0.65f; totalDSA >= 50 -> 0.40f; else -> totalDSA / 50f * 0.35f }
    val graph = topics["graphs"] ?: 0
    val dp = topics["dynamic_programming"] ?: 0
    val topicScore = minOf(1f, (graph / 15f * 0.5f) + (dp / 15f * 0.5f))
    val skillScore = minOf(1f, skills / 10f)
    val consistencyScore = minOf(1f, streak / 60f)
    val probability = ((dsaScore * 0.45f + topicScore * 0.25f + skillScore * 0.15f + consistencyScore * 0.15f) * 85f).toInt().coerceIn(2, 85)

    val actions = mutableListOf<String>()
    if (totalDSA < 150) actions += "Solve ${150 - totalDSA} more DSA problems"
    if (graph < 10) actions += "Practice ${10 - graph} graph problems"
    if (dp < 10) actions += "Practice ${10 - dp} DP problems"
    return TierProbability("Top Product Companies", "Google, Amazon, Flipkart, Microsoft", probability,
      listOf("150+ DSA problems (you have: $totalDSA)", "Strong graphs & DP", "System Design basics", "60+ day streak"), actions.take(2))
  }

  private fun calcTier2(totalDSA: Int, skills: Int, streak: Int, xp: Int): TierProbability {
    val dsaScore = minOf(1f, totalDSA / 80f)
    val skillScore = minOf(1f, skills / 7f)
    val consistencyScore = minOf(1f, streak / 30f)
    val probability = ((dsaScore * 0.5f + skillScore * 0.25f + consistencyScore * 0.25f) * 90f).toInt().coerceIn(5, 90)
    return TierProbability("Growth Startups", "Razorpay, CRED, Zepto, Swiggy", probability,
      listOf("80+ DSA problems", "5+ relevant skills", "30+ day streak"),
      if (totalDSA < 80) listOf("Solve ${80 - totalDSA} more problems") else emptyList())
  }

  private fun calcTier3(totalDSA: Int, skills: Int, xp: Int): TierProbability {
    val dsaScore = minOf(1f, totalDSA / 30f)
    val skillScore = minOf(1f, skills / 4f)
    val probability = ((dsaScore * 0.6f + skillScore * 0.4f) * 95f).toInt().coerceIn(10, 95)
    return TierProbability("Service Companies", "TCS, Infosys, Wipro, Cognizant", probability,
      listOf("30+ DSA problems", "Basic programming skills"), emptyList())
  }

  private fun identifyBottleneck(totalDSA: Int, topics: Map<String, Int>, skills: Int, streak: Int): String = when {
    totalDSA < 50 -> "DSA volume — solve ${50 - totalDSA} more problems first"
    (topics["graphs"] ?: 0) < 5 -> "Graph problems — critical for product companies"
    (topics["dynamic_programming"] ?: 0) < 5 -> "Dynamic programming — required for Tier 1 interviews"
    streak < 14 -> "Consistency — build a 14-day streak"
    skills < 5 -> "Skill diversity — add ${5 - skills} more skills"
    else -> "System Design — the next level for Tier 1"
  }
}
