package com.nexus.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * NEXUS 2.0 entities (schema v8). Field paths, nullability, and indices mirror
 * the exported schema in schemas/com.nexus.database.NexusDatabase/8.json so that
 * Room's post-migration validation matches Migration.kt's DDL.
 */

@Entity(
    tableName = "career_memory",
    indices = [Index("memoryType"), Index("createdAt"), Index("relevanceScore")],
)
data class CareerMemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memoryType: String,
    val content: String,
    val sourceConversationId: String? = null,
    val sourceTool: String? = null,
    val relevanceScore: Float = 1f,
    val isActive: Boolean = true,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastReferencedAt: Long = System.currentTimeMillis(),
    val referenceCount: Int = 0,
)

@Entity(
    tableName = "skill_nodes",
    indices = [Index("isUserKnown"), Index("category")],
)
data class SkillNodeEntity(
    @PrimaryKey val skillId: String,
    val displayName: String,
    val category: String,
    val difficultyLevel: Int = 2,
    val popularityScore: Float = 0.5f,
    val isUserKnown: Boolean = false,
    val userProficiency: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "skill_edges",
    primaryKeys = ["fromSkillId", "toSkillId"],
    indices = [Index("fromSkillId"), Index("toSkillId")],
)
data class SkillEdgeEntity(
    val fromSkillId: String,
    val toSkillId: String,
    val edgeType: String,
    val weight: Float,
    val transferability: Float = 0.5f,
)

@Entity(
    tableName = "opportunity_embeddings",
    indices = [Index(value = ["opportunityId", "opportunityType"], unique = true)],
)
data class OpportunityEmbeddingEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val opportunityId: String,
    val opportunityType: String,
    val embedding: ByteArray,
    val modelVersion: String,
    val cachedAt: Long = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpportunityEmbeddingEntity) return false
        return id == other.id && opportunityId == other.opportunityId &&
            opportunityType == other.opportunityType &&
            embedding.contentEquals(other.embedding) &&
            modelVersion == other.modelVersion && cachedAt == other.cachedAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + opportunityId.hashCode()
        result = 31 * result + opportunityType.hashCode()
        result = 31 * result + embedding.contentHashCode()
        result = 31 * result + modelVersion.hashCode()
        result = 31 * result + cachedAt.hashCode()
        return result
    }
}

@Entity(
    tableName = "retention_events",
    indices = [Index("installWeek"), Index("activityWeek")],
)
data class RetentionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val installWeek: String,
    val activityWeek: String,
    val weeksSinceInstall: Int,
    val sessionCount: Int,
    val xpEarned: Int,
    val featuresUsed: String,
    val recordedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "user_surveys",
    indices = [Index("surveyType")],
)
data class UserSurveyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val surveyType: String,
    val score: Int? = null,
    val responseText: String? = null,
    val triggerEvent: String,
    val daysActive: Int,
    val completedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "interview_outcomes",
    indices = [
        Index("company"),
        Index("interviewDate"),
        Index("result"),
        Index("applicationId"),
    ],
)
data class InterviewOutcomeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val applicationId: String,
    val company: String,
    val role: String,
    val interviewRound: String,
    val interviewDate: Long,
    val result: String,
    val questionsAsked: String = "",
    val topics: String = "",
    val confidenceRating: Float? = null,
    val perceivedPerformance: Float? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "confidence_assessments",
    indices = [Index("assessmentType"), Index("createdAt")],
)
data class ConfidenceSelfAssessmentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val assessmentType: String,
    val confidenceScore: Float,
    val actualScore: Float? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "placement_forecasts",
    indices = [Index("userId"), Index("companyTier"), Index("calculatedAt")],
)
data class PlacementForecastEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val companyTier: String,
    val probabilityScore: Float,
    val determiningFactors: String = "",
    val calculatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "career_mirrors",
    indices = [Index("userId"), Index("periodStart"), Index("createdAt")],
)
data class CareerMirrorEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val periodStart: Long,
    val periodEnd: Long,
    val periodLabel: String,
    val narrative: String = "",
    val keyInsights: String = "",
    val actionItems: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "score_snapshots",
    indices = [Index("userId"), Index("recordedAt")],
)
data class ScoreSnapshotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val score: Int,
    val components: String = "",
    val delta: Int = 0,
    val recordedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "cohort_intelligence",
    indices = [
        Index("collegeId"),
        Index("collegeId", "companyTier"),
        Index("semester"),
    ],
)
data class CohortIntelligenceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val collegeId: String,
    val semester: String,
    val companyTier: String,
    val totalStudents: Int = 0,
    val placedCount: Int = 0,
    val avgScore: Float = 0f,
    val avgStreak: Float = 0f,
    val skills: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "skill_velocity",
    primaryKeys = ["userId", "skillId"],
    indices = [Index("userId"), Index("recordedAt")],
)
data class SkillVelocitySnapshotEntity(
    val userId: String,
    val skillId: String,
    val skillName: String,
    val proficiency: Int,
    val previousProficiency: Int = 0,
    val velocityScore: Float = 0f,
    val daysSinceStart: Int = 0,
    val onTrackForTarget: Boolean = true,
    val recordedAt: Long,
)

@Entity(
    tableName = "interview_reports",
    indices = [Index("company"), Index("role"), Index("reportedAt")],
)
data class InterviewReportEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val applicationId: String,
    val company: String,
    val role: String,
    val roundType: String,
    val roundNumber: Int,
    val questions: String = "",
    val difficulty: String,
    val topics: String = "",
    val outcome: String,
    val tips: String? = null,
    val isAnonymized: Boolean = true,
    val reportedAt: Long = System.currentTimeMillis(),
)
