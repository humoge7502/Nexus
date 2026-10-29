package com.nexus.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewIntelligenceEngine @Inject constructor() {

  data class CompanyIntel(
    val company: String,
    val rounds: List<RoundIntel>,
    val weakAreasForUser: List<String>,
    val prepMessage: String,
    val questionQueries: List<String>,
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
      "microsoft" to listOf(
        RoundSpec("Coding — Arrays & Trees", "Medium", listOf("arrays", "trees", "strings")),
        RoundSpec("System Design", "Medium", listOf("system design", "scalability")),
        RoundSpec("CS Fundamentals", "Medium", listOf("OS", "DBMS", "networks")),
        RoundSpec("AA (As Appropriate)", "Easy", listOf("behavioral", "culture fit")),
      ),
      "amazon" to listOf(
        RoundSpec("Online Assessment", "Medium", listOf("arrays", "strings", "greedy")),
        RoundSpec("Coding 1", "Hard", listOf("graphs", "DP", "trees")),
        RoundSpec("Coding 2", "Hard", listOf("system design", "LLD")),
        RoundSpec("Bar Raiser", "Medium", listOf("leadership principles", "behavioral")),
      ),
      "meta" to listOf(
        RoundSpec("Coding 1", "Hard", listOf("arrays", "strings", "graphs")),
        RoundSpec("Coding 2", "Hard", listOf("trees", "DP", "heaps")),
        RoundSpec("System Design", "Hard", listOf("system design", "distributed", "caching")),
        RoundSpec("Behavioral", "Medium", listOf("behavioral", "culture fit")),
      ),
      "apple" to listOf(
        RoundSpec("Coding", "Hard", listOf("arrays", "strings", "DP")),
        RoundSpec("Domain Deep Dive", "Hard", listOf("OS", "memory", "concurrency")),
        RoundSpec("Hiring Manager", "Medium", listOf("behavioral", "projects")),
      ),
    )

    private val COMPANY_ALIASES = mapOf(
      "microsoft" to "microsoft",
      "msft" to "microsoft",
      "amazon" to "amazon",
      "aws" to "amazon",
      "meta" to "meta",
      "facebook" to "meta",
      "apple" to "apple",
      "google" to "google",
      "alphabet" to "google",
      "flipkart" to "flipkart",
      "swiggy" to "swiggy",
      "razorpay" to "razorpay",
    )

    val DEFAULT_ROUNDS = listOf(
      RoundSpec("DSA Round 1", "Medium", listOf("arrays", "strings", "DP")),
      RoundSpec("Technical Round", "Medium", listOf("OOPs", "DBMS", "OS")),
      RoundSpec("HR Round", "Easy", listOf("behavioral", "culture fit")),
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
    val normalized = normalizeCompany(company)
    val rounds = COMPANY_ROUND_MAP[normalized] ?: DEFAULT_ROUNDS

    val weaknessKeywords = userKnownWeaknesses.map { it.lowercase() }.filter { it.isNotBlank() }
    val roundsIntel = rounds.map { spec ->
      val missingInDsa = spec.topics.any { topic ->
        userDsaHistory.entries.any { (name, status) ->
          name.equals(topic, ignoreCase = true) &&
            (status.equals("NOT_ATTEMPTED", true) || status.equals("REVISIT", true))
        }
      }
      val matchesKnownWeakness = spec.topics.any { topic ->
        weaknessKeywords.any { weak -> weak.contains(topic, ignoreCase = true) || topic.contains(weak, ignoreCase = true) }
      }
      RoundIntel(
        roundName = spec.roundName,
        avgDifficulty = spec.difficulty,
        topics = spec.topics,
        userWeakSpotHere = missingInDsa || matchesKnownWeakness,
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

    return CompanyIntel(company, roundsIntel, weakAreasThere, prepMessage, questionQueries)
  }

  private fun normalizeCompany(company: String): String {
    val words = company.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
    return words.firstNotNullOfOrNull { COMPANY_ALIASES[it] } ?: "generic"
  }

  fun buildMockInterviewPrompt(company: String, role: String): String {
    val normalized = normalizeCompany(company)
    val rounds = COMPANY_ROUND_MAP[normalized] ?: DEFAULT_ROUNDS

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
}
