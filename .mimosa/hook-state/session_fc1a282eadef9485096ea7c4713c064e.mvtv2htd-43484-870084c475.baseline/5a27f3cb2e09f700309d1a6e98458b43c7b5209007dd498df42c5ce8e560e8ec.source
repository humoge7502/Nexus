package com.nexus.analytics

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

object FeatureFlagKeys {
  const val ONBOARDING_STEPS = "onboarding_steps"
  const val ONBOARDING_SHOW_DEMO = "onboarding_show_demo"
  const val ONBOARDING_SKILL_COUNT = "onboarding_skill_count"
  const val HOME_LAYOUT = "home_layout"
  const val HOME_SHOW_INSIGHT_CARD = "home_show_insight_card"
  const val HOME_CAREER_HIT_POSITION = "career_hit_position"
  const val AI_CHAT_GREETING = "ai_chat_greeting"
  const val AI_RATE_LIMIT_FREE = "ai_rate_limit_free"
  const val AI_SHOW_SUGGESTIONS = "ai_show_suggestions"
  const val XP_MULTIPLIER_WEEKENDS = "xp_multiplier_weekends"
  const val BADGE_CELEBRATION_STYLE = "badge_celebration"
  const val STREAK_FREEZE_DEFAULT_COUNT = "streak_freeze_count"
  const val NOTIFICATION_MORNING_TIME = "notification_morning_time"
  const val NOTIFICATION_EVENING_TIME = "notification_evening_time"
  const val ENABLE_RAG = "enable_rag"
  const val ENABLE_PROACTIVE_INSIGHTS = "enable_proactive"
  const val ENABLE_SKILL_GRAPH = "enable_skill_graph"
  const val ENABLE_PLACEMENT_PREDICTION = "enable_placement"
  const val ENABLE_CAREER_MIRROR = "enable_career_mirror"
  const val ENABLE_COHORT_INTELLIGENCE = "enable_cohort_intelligence"
  const val ENABLE_INTERVIEW_INTELLIGENCE = "enable_interview_intelligence"
  const val ENABLE_CONFIDENCE_CALIBRATION = "enable_confidence_calibration"
  const val ENABLE_TRIBE = "enable_tribe"
  const val ENABLE_SKILL_VELOCITY = "enable_skill_velocity"
  const val ENABLE_SCORE_PULSE = "enable_score_pulse"
  const val ENABLE_APPLICATION_MILESTONES = "enable_application_milestones"
}

object NEXUS2FeatureFlags {
  const val ENABLE_CAREER_MIRROR = "enable_career_mirror"
  const val ENABLE_COHORT_INTELLIGENCE = "enable_cohort_intelligence"
  const val ENABLE_INTERVIEW_INTELLIGENCE = "enable_interview_intelligence"
  const val ENABLE_CONFIDENCE_CALIBRATION = "enable_confidence_calibration"
  const val ENABLE_TRIBE = "enable_tribe"
  const val ENABLE_SKILL_VELOCITY = "enable_skill_velocity"
  const val ENABLE_SCORE_PULSE = "enable_score_pulse"
  const val ENABLE_APPLICATION_MILESTONES = "enable_application_milestones"
}

enum class HomeLayout { FEED, DASHBOARD }
enum class CelebrationStyle { TOAST, DIALOG, FULLSCREEN }

@Singleton
class NexusFeatureFlags @Inject constructor(
  private val analyticsTracker: AnalyticsTracker,
) {
  private val remoteConfig: FirebaseRemoteConfig by lazy {
    FirebaseRemoteConfig.getInstance().apply {
      setConfigSettingsAsync(
        FirebaseRemoteConfigSettings.Builder()
          .setMinimumFetchIntervalInSeconds(3600L)
          .build()
      )
      setDefaultsAsync(
        mapOf(
          FeatureFlagKeys.ONBOARDING_STEPS to "3",
          FeatureFlagKeys.ONBOARDING_SHOW_DEMO to false,
          FeatureFlagKeys.ONBOARDING_SKILL_COUNT to "5",
          FeatureFlagKeys.HOME_LAYOUT to "dashboard",
          FeatureFlagKeys.HOME_SHOW_INSIGHT_CARD to true,
          FeatureFlagKeys.HOME_CAREER_HIT_POSITION to "top",
          FeatureFlagKeys.AI_CHAT_GREETING to "casual",
          FeatureFlagKeys.AI_RATE_LIMIT_FREE to 10L,
          FeatureFlagKeys.AI_SHOW_SUGGESTIONS to false,
          FeatureFlagKeys.XP_MULTIPLIER_WEEKENDS to 1.5,
          FeatureFlagKeys.BADGE_CELEBRATION_STYLE to "dialog",
          FeatureFlagKeys.STREAK_FREEZE_DEFAULT_COUNT to 1L,
          FeatureFlagKeys.NOTIFICATION_MORNING_TIME to 7L,
          FeatureFlagKeys.NOTIFICATION_EVENING_TIME to 21L,
          FeatureFlagKeys.ENABLE_RAG to true,
          FeatureFlagKeys.ENABLE_PROACTIVE_INSIGHTS to true,
          FeatureFlagKeys.ENABLE_SKILL_GRAPH to true,
          FeatureFlagKeys.ENABLE_PLACEMENT_PREDICTION to true,
        )
      )
    }
  }

  suspend fun initialize() {
    try {
      remoteConfig.fetchAndActivate().await()
      Timber.d("Feature flags fetched and activated")
    } catch (e: Exception) {
      Timber.w(e, "Feature flag fetch failed — using defaults")
    }
  }

  fun getString(key: String): String = remoteConfig.getString(key)
  fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)
  fun getLong(key: String): Long = remoteConfig.getLong(key)
  fun getDouble(key: String): Double = remoteConfig.getDouble(key)

  val onboardingSteps: Int get() = getString(FeatureFlagKeys.ONBOARDING_STEPS).toIntOrNull() ?: 3
  val onboardingSkillCount: Int get() = getString(FeatureFlagKeys.ONBOARDING_SKILL_COUNT).let {
    if (it == "all") Int.MAX_VALUE else it.toIntOrNull() ?: 5
  }
  val homeLayout: HomeLayout get() = when (getString(FeatureFlagKeys.HOME_LAYOUT)) {
    "feed" -> HomeLayout.FEED
    else -> HomeLayout.DASHBOARD
  }
  val showInsightCard: Boolean get() = getBoolean(FeatureFlagKeys.HOME_SHOW_INSIGHT_CARD)
  val aiRateLimitFree: Int get() = getLong(FeatureFlagKeys.AI_RATE_LIMIT_FREE).toInt()
  val showAISuggestions: Boolean get() = getBoolean(FeatureFlagKeys.AI_SHOW_SUGGESTIONS)
  val weekendXPMultiplier: Float get() = getDouble(FeatureFlagKeys.XP_MULTIPLIER_WEEKENDS).toFloat()
  val badgeCelebrationStyle: CelebrationStyle get() = when (getString(FeatureFlagKeys.BADGE_CELEBRATION_STYLE)) {
    "toast" -> CelebrationStyle.TOAST
    "fullscreen" -> CelebrationStyle.FULLSCREEN
    else -> CelebrationStyle.DIALOG
  }
  val isRAGEnabled: Boolean get() = getBoolean(FeatureFlagKeys.ENABLE_RAG)
  val isProactiveInsightsEnabled: Boolean get() = getBoolean(FeatureFlagKeys.ENABLE_PROACTIVE_INSIGHTS)
  val isSkillGraphEnabled: Boolean get() = getBoolean(FeatureFlagKeys.ENABLE_SKILL_GRAPH)
  val isPlacementPredictionEnabled: Boolean get() = getBoolean(FeatureFlagKeys.ENABLE_PLACEMENT_PREDICTION)
  val morningNotificationHour: Int get() = getLong(FeatureFlagKeys.NOTIFICATION_MORNING_TIME).toInt()
  val eveningNotificationHour: Int get() = getLong(FeatureFlagKeys.NOTIFICATION_EVENING_TIME).toInt()

  fun trackExperimentExposure(experimentKey: String) {
    val variant = getString(experimentKey)
    analyticsTracker.track("experiment_exposure", mapOf("experiment" to experimentKey, "variant" to variant))
  }
}
