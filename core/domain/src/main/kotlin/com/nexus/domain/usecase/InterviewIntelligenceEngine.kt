package com.nexus.domain.usecase

import com.nexus.database.entity.InterviewOutcomeEntity
import com.nexus.domain.model.Internship
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewIntelligenceEngine @Inject constructor(
  private val scoreEngine: CareerScoreCalculator,
) {

  data class CompanyIntel(
    val company: String,
    val rounds: List<RoundIntel>,
    val weakAreasForUser: List<String>,
    val prepMessage: String,
    val questionQueries: List<String>,
    val successRate: Float,
  )

  data class RoundIntel(
    val roundName: String,
    val avgDifficulty: String,
    val topics: List<String>,
    val userWeakSpotHere: Boolean,
  )

  companion object {
    val COMPANY_ROUND_MAP = mapOf(
      "razorpay" to listOf(
        RoundSpec("DSA — Arrays & Strings", "Medium", listOf("arrays", "strings", "DP")),
        RoundSpec("System Design — E-commerce", "Hard", listOf("system design", "API design")),
        RoundSpec("DB + SQL", "Medium", listOf("SQL", "database", "normalization")),
        RoundSpec("HR + Values", "Easy", listOf("behavioral", "culture fit")),
      ),
      "flipkart" to listOf(
        RoundSpec("DSA — Graphs & Trees", "Hard", listOf("graphs", "trees", "DP")),
        RoundSpec("MS Internals", "Medium", listOf("OS", "networks", "OOPS")),
        RoundSpec("High-level Design", "Hard", listOf("HLD", "scalability", "distributed")),
        RoundSpec("HR", "Easy", listOf("behavioral")),
      ),
      "swiggy" to listOf(
        RoundSpec("DSA — Medium", "Medium", listOf("arrays", "strings", "greedy")),
        RoundSpec("Low-level Design", "Medium", listOf("LLD", "design patterns", "SOLID")),
        RoundSpec("HR", "Easy", listOf("behavioral")),
      ),
      "google" to listOf(
        RoundSpec("Googly 1", "Hard", listOf("DP", "graphs", "strings", "tries")),
        RoundSpec("Googly 2", "Hard", listOf("DP", "graphs", "math", "number theory")),
        RoundSpec("Googly 3", "Hard", listOf("system design", "distributed", "RPC")),
        RoundSpec("Googly 4", "Medium", listOf("Googleyness", "behavioral")),
      ),
    )

    val KNOWN_WEAK_TOPIC_KEYWORDS = mapOf(
      "DSA — Arrays & Strings" to listOf("arrays", "strings", "two pointer"),
      "DSA — Graphs & Trees" to listOf("graphs", "trees", "DFS", "BFS"),
      "System Design — E-commerce" to listOf("system design", "URL shortener"),
      "Low-level Design" to listOf("LLD", "design patterns", "SOLID"),
      "MS Internals" to listOf("OS", "networks", "OOPS"),
    )
  }

  data class RoundSpec(
    val roundName: String,
    val difficulty: String,
    val topics: List<String>,
  )

  fun generateCompanyIntel(
    company: String,
    userKnownWeaknesses: List<String>,
    userDsaHistory: Map<String, String>,
  ): CompanyIntel {
    val normalized = company.lowercase().split(" ").first { stop ->
      stop in setOf("razorpay", "flipkart", "swiggy", "google", "microsoft", "amazon", "meta", "apple")
    }.let {
      when (it) {
        "razorpay" -> "razorpay"
        "flipkart" -> "flipkart"
        "swiggy" -> "swiggy"
        "google" -> "google"
        "microsoft" -> "google"
        else -> "razorpay"
      }
    }
    val rounds = COMPANY_ROUND_MAP[normalized] ?: listOf(
      RoundSpec("DSA Round 1", "Medium", listOf("arrays", "strings", "DP")),
      RoundSpec("Technical Round", "Medium", listOf("OOPs", "DBMS", "OS")),
      RoundSpec("HR Round", "Easy", listOf("behavioral", "culture fit")),
    )

    val roundsIntel = rounds.map { spec ->
      val userWeakHere = knownWeakTopics().any { kw ->
        userKnownWeaknesses.any { ukw -> kw.contains(ukw, ignoreCase = true) } ||
          spec.topics.any { t -> userDsaHistory[t]?.let { status ->
            status.equals("NOT_ATTEMPTED", true) || status.equals("REVISIT", true)
          } ?: false }
      }
      RoundIntel(
        roundName = spec.roundName,
        avgDifficulty = spec.difficulty,
        topics = spec.topics,
        userWeakSpotHere = userWeakHere,
      )
    }

    val weakAreasThere = roundsIntel
      .filter { it.userWeakSpotHere }
      .flatMap { it.topics }
      .distinct()

    val prepMessage = if (weakAreasThere.isNotEmpty()) {
      "Your ${weakAreasThere.joinToString(", ")} prep needs attention before ${company.replaceFirstChar { it.uppercase() }} interview."
    } else {
      "Your profile looks aligned with ${company}'s typical interview rounds. Focus on communication."
    }

    val questionQueries = rounds.flatMap { it.topics }.distinct().take(5).map { topic ->
      "common ${company} interview questions on $topic"
    }

    val successRate = when (normalized) {
      "google" -> 0.02f
      "flipkart" -> 0.08f
      "swiggy" -> 0.12f
      "razorpay" -> 0.15f
      else -> 0.10f
    }

    return CompanyIntel(company, roundsIntel, weakAreasThere, prepMessage, questionQueries, successRate)
  }

  fun buildMockInterviewPrompt(company: String, role: String): String {
    val normalized = company.lowercase().split(" ").firstOrNull {
      it in setOf("razorpay", "flipkart", "swiggy", "google", "microsoft", "amazon")
    } ?: "generic"
    val rounds = COMPANY_ROUND_MAP[normalized] ?: listOf(
      RoundSpec("DSA", "Medium", listOf("arrays", "strings")),
      RoundSpec("Technical", "Medium", listOf("OOPs", "DBMS")),
      RoundSpec("HR", "Easy", listOf("behavioral")),
    )

    val roundList = rounds.joinToString("\n") { r ->
      "- ${r.roundName} (${r.difficulty}): Ask 2 questions on ${r.topics.joinToString(", ")}. Provide evaluation rubric."
    }
    return """
You are an AI Mock Interview Coach simulating a $role interview at $company.
Generate a realistic 3-question interview, one per round.
Structure:
$roundList
For each question:
1. State the question clearly
2. Provide hint at 30s if user is stuck
3. Provide ideal solution approach
4. Rate 1-5 after answer
Do NOT skip rounds. Be strict but fair.
    """.trimIndent()
  }

  private fun knownWeakTopics(): List<String> = KNOWN_WEAK_TOPIC_KEYWORDS.keys.toList()
}
