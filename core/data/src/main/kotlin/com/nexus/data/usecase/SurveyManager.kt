package com.nexus.data.usecase

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.UserSurveyDao
import com.nexus.database.entity.UserSurveyEntity
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class SurveyPrompt(val triggerEvent: String) {
  class SeanEllis : SurveyPrompt("session_start_day_")
  class NPS : SurveyPrompt("session_start_day_")
}

@Singleton
class SurveyManager @Inject constructor(
  private val surveyDao: UserSurveyDao,
  private val analyticsTracker: AnalyticsTracker,
) {
  companion object {
    private const val SEAN_ELLIS_MIN_DAYS = 7
    private const val NPS_MIN_DAYS = 14
    private const val MIN_SESSIONS_FOR_EARLY_SURVEY = 5
    private const val MIN_DAYS_BETWEEN_SURVEYS = 14
  }

  suspend fun evaluateSurveyEligibility(
    daysActive: Int,
    sessionCount: Int,
  ): SurveyPrompt? {
    val surveyCount = surveyDao.getCount()
    if (surveyCount > 0) {
      val allSurveys = surveyDao.getAll().first()
      val mostRecent = allSurveys.maxByOrNull { it.completedAt } ?: return null
      val daysSinceLast = (System.currentTimeMillis() - mostRecent.completedAt) / (1000 * 60 * 60 * 24)
      if (daysSinceLast.toInt() < MIN_DAYS_BETWEEN_SURVEYS) return null
    }

    if (daysActive >= SEAN_ELLIS_MIN_DAYS) {
      val isEarlyEligible = daysActive >= SEAN_ELLIS_MIN_DAYS && sessionCount >= MIN_SESSIONS_FOR_EARLY_SURVEY
      val isLateEligible = daysActive >= 21
      if (isEarlyEligible || isLateEligible) {
        val prompt = SurveyPrompt.SeanEllis()
        return prompt
      }
    }

    if (daysActive >= NPS_MIN_DAYS) {
      return SurveyPrompt.NPS()
    }

    return null
  }

  suspend fun recordResponse(
    surveyType: String,
    score: Int?,
    responseText: String?,
    triggerEvent: String,
    daysActive: Int,
  ) {
    val entity = UserSurveyEntity(
      surveyType = surveyType,
      score = score,
      responseText = responseText,
      triggerEvent = triggerEvent,
      daysActive = daysActive,
    )
    surveyDao.insert(entity)

    val eventName = when (surveyType) {
      "SEAN_ELLIS" -> "sean_ellis_response"
      "NPS" -> "nps_response"
      else -> "survey_response"
    }
    analyticsTracker.track(
      eventName,
      mapOf(
        "score" to (score?.toString() ?: "-1"),
        "has_text" to (responseText != null).toString(),
        "days_active" to daysActive.toString(),
        "trigger" to triggerEvent,
      )
    )
    Timber.d("Survey recorded: $surveyType score=$score")
  }
}
