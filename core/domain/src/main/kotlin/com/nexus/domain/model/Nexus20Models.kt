package com.nexus.domain.model

/**
 * NEXUS 2.0 domain models consumed by the repository layer and feature modules.
 */

data class CareerMirror(
    val id: String = "",
    val userId: String = "",
    val periodStart: Long = 0,
    val periodEnd: Long = 0,
    val periodLabel: String = "",
    val narrative: String = "",
    val keyInsights: List<String> = emptyList(),
    val actionItems: List<String> = emptyList(),
    val createdAt: Long = 0,
)

data class CohortEntry(
    val id: String = "",
    val collegeId: String = "",
    val semester: String = "",
    val companyTier: String = "",
    val totalStudents: Int = 0,
    val placedCount: Int = 0,
    val avgScore: Float = 0f,
    val avgStreak: Float = 0f,
    val topSkills: List<String> = emptyList(),
    val updatedAt: Long = 0,
)

data class PlacementForecast(
    val id: String = "",
    val userId: String = "",
    val companyTier: String = "",
    val probabilityScore: Float = 0f,
    val determiningFactors: List<String> = emptyList(),
    val calculatedAt: Long = 0,
)

data class InterviewOutcome(
    val id: String = "",
    val applicationId: String = "",
    val company: String = "",
    val role: String = "",
    val interviewRound: String = "",
    val interviewDate: Long = 0,
    val result: String = "",
    val questionsAsked: String = "",
    val topics: String = "",
    val confidenceRating: Float? = null,
    val perceivedPerformance: Float? = null,
    val notes: String = "",
    val createdAt: Long = 0,
)

data class ConfidenceAssessment(
    val id: String = "",
    val assessmentType: String = "",
    val confidenceScore: Float = 0f,
    val actualScore: Float? = null,
    val notes: String = "",
    val createdAt: Long = 0,
)

data class SkillVelocity(
    val userId: String = "",
    val skillId: String = "",
    val skillName: String = "",
    val proficiency: Int = 0,
    val previousProficiency: Int = 0,
    val velocityScore: Float = 0f,
    val daysSinceStart: Int = 0,
    val onTrackForTarget: Boolean = true,
    val recordedAt: Long = 0,
)

data class ScoreSnapshot(
    val id: String = "",
    val userId: String = "",
    val score: Int = 0,
    val componentsJson: String = "",
    val delta: Int = 0,
    val recordedAt: Long = 0,
)
