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

class AnalyticsTracker @Inject constructor() {
    fun track(event: AnalyticsEvent, properties: Map<String, String> = emptyMap()) {
        // In production, send to Firebase Analytics / Mixpanel
        android.util.Log.d("Analytics", "${event.eventName}: $properties")
    }

    fun trackScreenView(screenName: String) {
        track(AnalyticsEvent.SCREEN_VIEW, mapOf("screen" to screenName))
    }
}
