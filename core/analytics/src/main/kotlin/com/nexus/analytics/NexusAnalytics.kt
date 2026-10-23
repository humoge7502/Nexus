package com.nexus.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NexusAnalytics @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
) {
    fun trackCareerHitCompleted(hitType: String, xpAwarded: Int) {
        analyticsTracker.track("career_hit_completed", mapOf(
            "hit_type" to hitType, "xp_awarded" to xpAwarded,
        ))
    }

    fun trackOpportunitySaved(opportunityId: String, company: String, matchScore: Float) {
        analyticsTracker.track("opportunity_saved", mapOf(
            "opportunity_id" to opportunityId, "company" to company, "match_score" to matchScore,
        ))
    }

    fun trackOpportunityApplied(opportunityId: String, company: String) {
        analyticsTracker.track("opportunity_applied", mapOf(
            "opportunity_id" to opportunityId, "company" to company,
        ))
    }

    fun trackApplicationStatusChanged(applicationId: String, oldStatus: String, newStatus: String, company: String) {
        analyticsTracker.track("application_status_changed", mapOf(
            "id" to applicationId, "old_status" to oldStatus, "new_status" to newStatus, "company" to company,
        ))
    }

    fun trackDSAProblemSolved(difficulty: String, topic: String, timeSpent: Long?) {
        analyticsTracker.track("dsa_problem_solved", mapOf(
            "difficulty" to difficulty, "topic" to topic, "time_spent_ms" to (timeSpent ?: -1),
        ))
    }

    fun trackAIMessageSent(tool: String, messageLength: Int, isContextual: Boolean) {
        analyticsTracker.track("ai_message_sent", mapOf(
            "tool" to tool, "message_length" to messageLength, "is_contextual" to isContextual,
        ))
    }

    fun trackAIResponseReceived(tool: String, responseLength: Int, hadWarnings: Boolean, latencyMs: Long) {
        analyticsTracker.track("ai_response_received", mapOf(
            "tool" to tool, "response_length" to responseLength,
            "had_warnings" to hadWarnings, "latency_ms" to latencyMs,
        ))
    }

    fun trackAIRateLimitHit(tool: String, callsToday: Int) {
        analyticsTracker.track("ai_rate_limit_hit", mapOf(
            "tool" to tool, "calls_today" to callsToday,
        ))
    }

    fun trackXPAwarded(source: String, amount: Int, wasBonus: Boolean, newTotal: Int) {
        analyticsTracker.track("xp_awarded", mapOf(
            "source" to source, "amount" to amount, "was_bonus" to wasBonus, "new_total" to newTotal,
        ))
    }

    fun trackLevelUp(newLevel: String, totalXP: Int) {
        analyticsTracker.track("level_up", mapOf(
            "new_level" to newLevel, "total_xp" to totalXP,
        ))
    }

    fun trackBadgeUnlocked(badgeId: String, badgeName: String) {
        analyticsTracker.track("badge_unlocked", mapOf(
            "badge_id" to badgeId, "badge_name" to badgeName,
        ))
    }

    fun trackStreakMaintained(length: Int, freezesUsed: Int) {
        analyticsTracker.track("streak_maintained", mapOf(
            "length" to length, "freezes_used" to freezesUsed,
        ))
    }

    fun trackStreakBroken(length: Int) {
        analyticsTracker.track("streak_broken", mapOf("length_was" to length))
    }

    fun trackFeatureUsed(feature: String, durationMs: Long) {
        analyticsTracker.track("feature_used", mapOf(
            "feature" to feature, "duration_ms" to durationMs,
        ))
    }

    fun trackShareAction(contentType: String, platform: String?) {
        analyticsTracker.track("share_action", mapOf(
            "content_type" to contentType, "platform" to (platform ?: "unknown"),
        ))
    }

    fun trackWidgetInteraction(action: String) {
        analyticsTracker.track("widget_interaction", mapOf("action" to action))
    }

    fun trackSessionStarted(source: String, timeSinceLastMs: Long) {
        analyticsTracker.track("session_started", mapOf(
            "source" to source, "time_since_last_ms" to timeSinceLastMs,
        ))
    }

fun trackSessionEnded(durationMs: Long) {
  analyticsTracker.track("session_ended", mapOf("duration_ms" to durationMs))
}

fun trackCareerMirrorViewed(periodLabel: String, scoreDelta: Int) {
  analyticsTracker.track("career_mirror_viewed", mapOf(
    "period_label" to periodLabel, "score_delta" to scoreDelta,
  ))
}
fun trackCareerMirrorGenerated(insightCount: Int, actionItemCount: Int) {
  analyticsTracker.track("career_mirror_generated", mapOf(
    "insight_count" to insightCount, "action_item_count" to actionItemCount,
  ))
}

fun trackPlacementForecastViewed(tier: String, probabilityPct: Int) {
  analyticsTracker.track("placement_forecast_viewed", mapOf(
    "tier" to tier, "probability_pct" to probabilityPct,
  ))
}
fun trackPlacementForecastGenerated(tiersGenerated: Int) {
  analyticsTracker.track("placement_forecast_generated", mapOf("tiers_generated" to tiersGenerated))
}

fun trackInterviewOutcomeLogged(company: String, round: String, result: String, hasConfidenceRating: Boolean) {
  analyticsTracker.track("interview_outcome_logged", mapOf(
    "company" to company, "round" to round, "result" to result,
    "has_confidence_rating" to hasConfidenceRating,
  ))
}
fun trackInterviewIntelViewed(company: String, weaknessCount: Int) {
  analyticsTracker.track("interview_intel_viewed", mapOf(
    "company" to company, "weakness_count" to weaknessCount,
  ))
}

fun trackConfidenceAssessmentLogged(type: String, confidenceScore: Float, hasActualScore: Boolean) {
  analyticsTracker.track("confidence_assessment_logged", mapOf(
    "type" to type, "confidence_score" to confidenceScore, "has_actual_score" to hasActualScore,
  ))
}
fun trackCalibrationInsightReceived(biasDirection: String) {
  analyticsTracker.track("calibration_insight_received", mapOf("bias_direction" to biasDirection))
}

fun trackCohortIntelligenceViewed(collegeId: String, tier: String) {
  analyticsTracker.track("cohort_intelligence_viewed", mapOf(
    "college_id" to collegeId, "tier" to tier,
  ))
}

fun trackTribeJoined(collegeId: String, podSize: Int) {
  analyticsTracker.track("tribe_joined", mapOf("college_id" to collegeId, "pod_size" to podSize))
}
fun trackTribeInviteSent(toCollegeId: String) {
  analyticsTracker.track("tribe_invite_sent", mapOf("to_college_id" to toCollegeId))
}

fun trackScorePulseTriggered(delta: Int, newScore: Int) {
  analyticsTracker.track("score_pulse_triggered", mapOf("delta" to delta, "new_score" to newScore))
}

fun trackInterviewPrepStarted(company: String, role: String, hasIntel: Boolean) {
  analyticsTracker.track("interview_prep_started", mapOf(
    "company" to company, "role" to role, "has_intel" to hasIntel,
  ))
}

fun trackSkillVelocityViewed(skillId: String, velocityScore: Float, onTrack: Boolean) {
  analyticsTracker.track("skill_velocity_viewed", mapOf(
    "skill_id" to skillId, "velocity_score" to velocityScore, "on_track" to onTrack,
  ))
}

fun trackNexusScoreShared(platform: String?) {
  analyticsTracker.track("nexus_score_shared", mapOf("platform" to (platform ?: "unknown")))
}
}
