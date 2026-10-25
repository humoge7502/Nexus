package com.nexus.app

import kotlinx.serialization.Serializable

sealed interface NexusDestination {
    @Serializable data object Onboarding : NexusDestination
    @Serializable data object Home : NexusDestination
    @Serializable data object Explore : NexusDestination
    @Serializable data object AIHub : NexusDestination
    @Serializable data object Growth : NexusDestination
    @Serializable data object Goals : NexusDestination
    @Serializable data object DSATracker : NexusDestination
    @Serializable data object Notifications : NexusDestination
    @Serializable data object Leaderboard : NexusDestination
    @Serializable data class InternshipDetail(val internshipId: String) : NexusDestination
    @Serializable data class CertificationDetail(val certId: String) : NexusDestination
    @Serializable data object InternshipList : NexusDestination
    @Serializable data object CertificationBrowser : NexusDestination
    @Serializable data object Productivity : NexusDestination
    @Serializable data object Profile : NexusDestination
    @Serializable data object Achievements : NexusDestination
    @Serializable data object Settings : NexusDestination

    // AI sub-screens
    @Serializable data object AIChat : NexusDestination
    @Serializable data object AIResumeAnalyzer : NexusDestination

    // Sub-screens
    @Serializable data object Auth : NexusDestination

    // Hackathon
    @Serializable data class HackathonDetail(val hackathonId: String) : NexusDestination

    // NEXUS 2.0 new features
    @Serializable data object Oracle : NexusDestination
    @Serializable data object Arena : NexusDestination

    // Career intelligence & community tooling
    @Serializable data object Applications : NexusDestination
    @Serializable data object CareerMirror : NexusDestination
    @Serializable data object CohortIntelligence : NexusDestination
    @Serializable data object Community : NexusDestination
    @Serializable data object InterviewLog : NexusDestination
    @Serializable data object PlacementForecast : NexusDestination
    @Serializable data object Portfolio : NexusDestination
    @Serializable data object Search : NexusDestination
    @Serializable data object Tribe : NexusDestination

    // Monetization
    @Serializable data object Paywall : NexusDestination

    // Deep Research (agent-search pattern)
    @Serializable data object Research : NexusDestination
}
