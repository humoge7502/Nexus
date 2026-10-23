package com.nexus.domain.usecase

import com.nexus.database.dao.InterviewReportDao
import com.nexus.database.entity.InterviewReportEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewIntelligenceProvider @Inject constructor(
  private val interviewReportDao: InterviewReportDao,
  private val firestore: com.google.firebase.firestore.FirebaseFirestore,
) {

  data class CompanyIntel(
    val company: String,
    val totalReports: Int,
    val topTopics: List<String>,
    val roundDistribution: Map<String, Int>,
    val tips: List<String>,
    val hasLocalData: Boolean,
  )

  suspend fun getIntelligenceFor(company: String): CompanyIntel {
    val localReports = interviewReportDao.getReportsForCompany(company)
    val localCount = interviewReportDao.getReportCountForCompany(company)

    val successTopics = interviewReportDao.getSuccessfulTopicsForCompany(company)
      .flatMap { it.split(",") }
      .map { it.trim() }
      .groupBy { it }
      .mapValues { it.value.size }
      .entries.sortedByDescending { it.value }
      .take(5)
      .map { it.key }

    val roundDistribution = localReports.groupBy { it.roundType }.mapValues { it.value.size }
    val tips = localReports.mapNotNull { it.tips }.filter { it.length > 20 }.take(3)

    return CompanyIntel(company, localCount, successTopics, roundDistribution, tips, localReports.isNotEmpty())
  }

  suspend fun buildContext(company: String, role: String): String {
    val intel = getIntelligenceFor(company)
    if (!intel.hasLocalData) return ""
    return buildString {
      appendLine("=== NEXUS INTERVIEW INTELLIGENCE: $company ===")
      appendLine("Based on ${intel.totalReports} reported interviews")
      if (intel.topTopics.isNotEmpty()) {
        appendLine("Most tested topics (from successful candidates):")
        intel.topTopics.forEachIndexed { i, t -> appendLine(" ${i + 1}. $t") }
      }
      if (intel.roundDistribution.isNotEmpty()) {
        appendLine("Typical round structure:")
        intel.roundDistribution.entries.sortedByDescending { it.value }.forEach { (type, count) ->
          appendLine(" • $type ($count reported)")
        }
      }
      if (intel.tips.isNotEmpty()) {
        appendLine("Tips from students:")
        intel.tips.forEachIndexed { i, tip -> appendLine(" ${i + 1}. $tip") }
      }
      appendLine("=== END INTELLIGENCE ===")
    }
  }
}
