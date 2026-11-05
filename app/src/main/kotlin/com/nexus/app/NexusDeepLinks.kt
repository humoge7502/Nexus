package com.nexus.app

import android.content.Intent

/**
 * Maps push-notification payloads and `sagitta://` URIs to a destination.
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
            "hackathon" ->
                targetId?.let { NexusDestination.HackathonDetail(it) } ?: NexusDestination.Explore
            "streak" -> NexusDestination.Productivity
            "social" -> NexusDestination.Community
            "insight" -> NexusDestination.AIChat
            else -> targetId?.takeIf { it.isNotBlank() }?.let { NexusDestination.InternshipDetail(it) }
        }
    }

    /**
     * The group-chat bridge: extracts searchable text from an ACTION_SEND
     * share so a forwarded listing can be checked against the verified
     * catalog. Returns null for non-share intents or empty shares.
     *
     * Shared listing text is noisy (appended "Forwarded as received" lines,
     * emoji, trailing URLs). The URL is dropped because the verified catalog
     * is searched by company/role keywords, not by external link.
     */
    fun extractShareQuery(intent: Intent?): String? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND) return null
        val raw = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val cleaned = raw
            .lineSequence()
            // Share chains carry forward-counters and forwarding noise;
            // both are stripped before the text becomes a search query.
            .filterNot { line ->
                val l = line.trim()
                l.isEmpty() || l.startsWith("http") || l.startsWith("www.") ||
                    l.equals("Forwarded as received", ignoreCase = true) ||
                    Regex("^[\\p{So}\\p{Sk}\\p{Cf}\\s]+$").matches(l)
            }
            .joinToString(" ") { it.trim() }
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.takeIf { it.length >= 3 }?.take(120)
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
        "search" -> NexusDestination.Search()
        "community" -> NexusDestination.Community
        "tribe" -> NexusDestination.Tribe
        "ai", "ai-hub" -> NexusDestination.AIHub
        "chat" -> NexusDestination.AIChat
        "roadmap" -> NexusDestination.Roadmap
        "skill-gap" -> NexusDestination.SkillGap
        "linkedin" -> NexusDestination.LinkedInPost
        "interview-coach", "coach" -> NexusDestination.InterviewCoach
        "insights" -> NexusDestination.CareerInsights
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
