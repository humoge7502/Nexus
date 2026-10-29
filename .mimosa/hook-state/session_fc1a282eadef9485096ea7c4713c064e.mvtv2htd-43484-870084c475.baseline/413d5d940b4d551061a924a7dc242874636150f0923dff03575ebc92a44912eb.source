package com.nexus.analytics

import javax.inject.Inject

enum class AnalyticsEvent(val eventName: String) {
    SCREEN_VIEW("screen_view"),
    INTERNSHIP_VIEWED("internship_viewed"),
    INTERNSHIP_APPLIED("internship_applied"),
    INTERNSHIP_SAVED("internship_saved"),
    CERTIFICATION_ENROLLED("certification_enrolled"),
    CERTIFICATION_COMPLETED("certification_completed"),
    POMODORO_COMPLETED("pomodoro_completed"),
    ACHIEVEMENT_UNLOCKED("achievement_unlocked"),
    THEME_CHANGED("theme_changed"),
    SEARCH_PERFORMED("search_performed"),
}

enum class ResearchEvent(val eventName: String) {
    AIRateLimitHit("ai_rate_limit_hit"),
    AISessionStarted("ai_session_started"),
    AIResponseReceived("ai_response_received"),
    AIResponseRated("ai_response_rated"),
    AIInsightAction("ai_insight_action"),
}

class AnalyticsTracker @Inject constructor() {

    fun trackResearchEvent(event: ResearchEvent, properties: Map<String, Any> = emptyMap()) {
        track(event.eventName, properties)
    }

    /** Free-form event tracking used across feature modules. */
    fun track(eventName: String, properties: Map<String, Any> = emptyMap()) {
        // In production, forward to Firebase Analytics:
        //   Firebase.analytics.logEvent(eventName, properties.toBundle())
        android.util.Log.d("Analytics", "$eventName: $properties")
    }

    /** Typed tracking for the canonical product events. */
    fun track(event: AnalyticsEvent, properties: Map<String, Any> = emptyMap()) {
        track(event.eventName, properties)
    }

    fun trackScreenView(screenName: String) {
        track(AnalyticsEvent.SCREEN_VIEW, mapOf("screen" to screenName))
    }
}
