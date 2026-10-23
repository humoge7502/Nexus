package com.nexus.data.usecase

import com.nexus.data.repository.UserXpRepository
import com.nexus.database.dao.RetentionDao
import com.nexus.database.entity.RetentionEntity
import com.nexus.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetentionTracker @Inject constructor(
  private val retentionDao: RetentionDao,
  private val userPreferences: UserPreferences,
  private val xpRepository: UserXpRepository,
  private val analyticsTracker: com.nexus.analytics.AnalyticsTracker,
) {
  companion object {
    fun getCurrentISOWeek(): String {
      val cal = Calendar.getInstance()
      cal.minimalDaysInFirstWeek = 4
      val week = cal.get(Calendar.WEEK_OF_YEAR)
      return "${cal.get(Calendar.YEAR)}-W${week.toString().padStart(2, '0')}"
    }

    fun getInstallISOWeek(installDate: LocalDate): String {
      val week = installDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
      val year = installDate.get(IsoFields.WEEK_BASED_YEAR)
      return "$year-W${week.toString().padStart(2, '0')}"
    }

    fun weeksBetween(fromWeek: String, toWeek: String): Int {
      val fromParts = fromWeek.split("-W")
      val toParts = toWeek.split("-W")
      if (fromParts.size != 2 || toParts.size != 2) return 0
      val (fromYear, fromW) = fromParts.map { it.toIntOrNull() ?: return 0 }
      val (toYear, toW) = toParts.map { it.toIntOrNull() ?: return 0 }
      return (toYear - fromYear) * 52 + (toW - fromW)
    }
  }

  suspend fun recordWeeklyActivity(sessionCount: Int, featuresUsed: List<String>) {
    val installDateStr = userPreferences.lastLoginDate.first()
    val installDate = if (installDateStr.isNotBlank()) {
      try {
        LocalDate.parse(installDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
      } catch (e: Exception) {
        LocalDate.now().minusDays(1)
      }
    } else {
      LocalDate.now()
    }

    val installWeek = getInstallISOWeek(installDate)
    val currentWeek = getCurrentISOWeek()
    val weekNumber = weeksBetween(installWeek, currentWeek)
    val weeklyXP = xpRepository.totalXP.first()

    val event = RetentionEntity(
      installWeek = installWeek,
      activityWeek = currentWeek,
      weeksSinceInstall = weekNumber,
      sessionCount = sessionCount,
      xpEarned = weeklyXP,
      featuresUsed = featuresUsed.joinToString(","),
    )

    retentionDao.insert(event)

    analyticsTracker.track(
      "weekly_activity_recorded",
      mapOf(
        "install_week" to installWeek,
        "weeks_since_install" to weekNumber.toString(),
        "session_count" to sessionCount.toString(),
        "xp_earned" to weeklyXP.toString(),
      )
    )

    Timber.d("Retention: Week $weekNumber since install, $sessionCount sessions, $weeklyXP XP")
  }

  suspend fun getRetentionTrajectory(): List<RetentionEntity> {
    val installWeek = retentionDao.getInstallWeek() ?: return emptyList()
    return retentionDao.getCohortData(installWeek).first()
  }
}
