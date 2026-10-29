package com.nexus.app

import android.content.Intent

/**
 * Maps push-notification payloads and `nexus://` URIs to a destination.
 * Both entry points are untrusted, so unknown values are ignored rather than
 * being turned into a navigation crash.
 */
object NexusDeepLinks {

    fun resolve(intent: Intent?): NexusDestination? {
        if (intent == null) return null

        intent.data?.let { uri ->
            val segments = buildList {
                uri.host?.takeIf { it.isNotBlank() }?.let(::add)
                uri.pathSegments.filterTo(this) { it.isNotBlank() }
            }
            if (segments.isNotEmpty()) {
                resolveRoute(segments.first().lowercase(), segments.getOrNull(1))?.let { return it }
            }
        }

        val targetId = intent.getStringExtra("target_id")
        return when (intent.getStringExtra("notification_type")?.lowercase()) {
            "opportunity" ->
                targetId?.let { NexusDestination.InternshipDetail(it) } ?: NexusDestination.InternshipList
            "deadline" ->
                targetId?.let { NexusDestination.InternshipDetail(it) } ?: NexusDestination.Applications
            "streak" -> NexusDestination.Productivity
            "social" -> NexusDestination.Community
            "insight" -> NexusDestination.AIChat
            else -> targetId?.takeIf { it.isNotBlank() }?.let { NexusDestination.InternshipDetail(it) }
        }
    }

    private fun resolveRoute(head: String, id: String?): NexusDestination? = when (head) {
        "internship", "internships", "opportunity" ->
            id?.let { NexusDestination.InternshipDetail(it) } ?: NexusDestination.InternshipList
        "certification", "certifications" ->
            id?.let { NexusDestination.CertificationDetail(it) } ?: NexusDestination.CertificationBrowser
        "hackathon", "hackathons" -> id?.let { NexusDestination.HackathonDetail(it) }
        "profile" -> NexusDestination.Profile
        "settings" -> NexusDestination.Settings
        "applications" -> NexusDestination.Applications
        "forecast", "placement", "placement-forecast" -> NexusDestination.PlacementForecast
        "mirror", "career-mirror", "careermirror" -> NexusDestination.CareerMirror
        "cohort", "cohort-intelligence" -> NexusDestination.CohortIntelligence
        "interview", "interviews", "interview-log" -> NexusDestination.InterviewLog
        "portfolio" -> NexusDestination.Portfolio
        "search" -> NexusDestination.Search
        "community" -> NexusDestination.Community
        "tribe" -> NexusDestination.Tribe
        "ai", "ai-hub" -> NexusDestination.AIHub
        "chat" -> NexusDestination.AIChat
        "growth" -> NexusDestination.Growth
        "productivity", "focus", "pomodoro" -> NexusDestination.Productivity
        "notifications" -> NexusDestination.Notifications
        "achievements" -> NexusDestination.Achievements
        "leaderboard" -> NexusDestination.Leaderboard
        "goals" -> NexusDestination.Goals
        "explore" -> NexusDestination.Explore
        "home" -> NexusDestination.Home
        "pro", "paywall" -> NexusDestination.Paywall
        else -> null
    }
}
