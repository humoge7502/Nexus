package com.nexus.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Base entities (schema v1→v8). Index declarations mirror
 * schemas/com.nexus.database.NexusDatabase/8.json so post-migration
 * validation passes; Migration.kt backfills them on the v2→v8 path.
 */

@Entity(
    tableName = "internships",
    indices = [
        Index("is_saved"),
        Index("deadline"),
        Index("company_name"),
        Index("cached_at"),
        Index("posted_at"),
    ],
)
data class InternshipEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "company_name") val companyName: String,
    @ColumnInfo(name = "company_logo_url") val companyLogoUrl: String? = null,
    val location: String,
    @ColumnInfo(name = "is_remote") val isRemote: Boolean,
    @ColumnInfo(name = "stipend_amount") val stipendAmount: Int? = null,
    @ColumnInfo(name = "stipend_currency") val stipendCurrency: String = "INR",
    @ColumnInfo(name = "duration_months") val durationMonths: Int,
    val skills: String = "", // JSON array
    val deadline: Long = 0, // epoch millis
    @ColumnInfo(name = "apply_url") val applyUrl: String,
    val description: String = "",
    @ColumnInfo(name = "posted_at") val postedAt: Long = 0,
    @ColumnInfo(name = "is_saved") val isSaved: Boolean = false,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "certifications",
    indices = [Index("provider"), Index("is_trending")],
)
data class CertificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    @ColumnInfo(name = "estimated_hours") val estimatedHours: Int,
    val skills: String = "",
    val rating: Float = 0f,
    @ColumnInfo(name = "enrollment_url") val enrollmentUrl: String = "",
    @ColumnInfo(name = "is_trending") val isTrending: Boolean = false,
    @ColumnInfo(name = "employer_recognition") val employerRecognition: Int = 0,
)

@Entity(tableName = "certification_progress")
data class CertificationProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "certification_id") val certificationId: String,
    @ColumnInfo(name = "enrolled_at") val enrolledAt: Long? = null,
    @ColumnInfo(name = "target_completion_date") val targetCompletionDate: String? = null,
    @ColumnInfo(name = "completed_modules") val completedModules: Int = 0,
    @ColumnInfo(name = "total_modules") val totalModules: Int = 0,
    @ColumnInfo(name = "study_hours_logged") val studyHoursLogged: Int = 0,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "certificate_url") val certificateUrl: String? = null,
)

@Entity(
    tableName = "applications",
    indices = [Index("status"), Index("company"), Index("opportunity_id")],
)
data class ApplicationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "opportunity_id") val opportunityId: String,
    val company: String,
    val role: String,
    @ColumnInfo(name = "opportunity_type") val opportunityType: String,
    val status: String = "SAVED",
    @ColumnInfo(name = "match_score") val matchScore: Float = 0f,
    @ColumnInfo(name = "applied_date") val appliedDate: Long? = null,
    @ColumnInfo(name = "interview_date") val interviewDate: Long? = null,
    @ColumnInfo(name = "interview_rounds") val interviewRounds: Int = 0,
    @ColumnInfo(name = "outcome_date") val outcomeDate: Long? = null,
    @ColumnInfo(name = "outcome_notes") val outcomeNotes: String? = null,
    @ColumnInfo(name = "salary_offered") val salaryOffered: String? = null,
    @ColumnInfo(name = "preparation_notes") val preparationNotes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "study_sessions",
    indices = [Index("start_time")],
)
data class StudySessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    val topic: String = "",
    @ColumnInfo(name = "focus_score") val focusScore: Float = 1f,
)

@Entity(
    tableName = "achievements",
    indices = [Index("is_unlocked"), Index("type")],
)
data class AchievementEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean = false,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long? = null,
    val progress: Float = 0f,
    @ColumnInfo(name = "max_progress") val maxProgress: Float = 1f,
    @ColumnInfo(name = "xp_reward") val xpReward: Int = 0,
)

@Entity(
    tableName = "hackathons",
    indices = [
        Index("isLive"),
        Index("isSaved"),
        Index("startDate"),
        Index("registrationDeadline"),
    ],
)
data class HackathonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organizer: String,
    val description: String = "",
    val mode: String = "ONLINE",
    val location: String? = null,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val registrationDeadline: Long = 0,
    val prizePool: String = "",
    val registrationUrl: String = "",
    val isLive: Boolean = false,
    val isSaved: Boolean = false,
    val tags: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "scholarships",
    indices = [Index("isOpen"), Index("deadline")],
)
data class ScholarshipEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    val amount: String,
    val description: String = "",
    val eligibility: String = "",
    val deadline: Long = 0,
    val applicationUrl: String = "",
    val isOpen: Boolean = true,
    val tags: String = "",
)

@Entity(
    tableName = "projects",
    indices = [Index("isFeatured"), Index("category")],
)
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val techStack: String = "",
    val githubUrl: String? = null,
    val liveUrl: String? = null,
    val imageUrl: String? = null,
    val starCount: Int = 0,
    val isFeatured: Boolean = false,
    val completedAt: Long? = null,
    val category: String = "GENERAL",
)

@Entity(
    tableName = "goals",
    indices = [Index("isCompleted"), Index("category"), Index("targetDate")],
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val category: String,
    val progress: Float = 0f,
    val targetDate: Long? = null,
    val milestones: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)

@Entity(
    tableName = "dsa_problems",
    indices = [Index("difficulty"), Index("status"), Index("platform")],
)
data class DSAProblemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val difficulty: String,
    val category: String = "",
    val platform: String = "LeetCode",
    val link: String = "",
    val topics: String = "",
    val frequency: Float = 0f,
    val status: String = "NOT_ATTEMPTED",
    val solvedAt: Long? = null,
    val userNotes: String = "",
)

@Entity(
    tableName = "notifications",
    indices = [Index("isRead"), Index("createdAt"), Index("type")],
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "ai_conversations",
    indices = [Index("lastMessageAt")],
)
data class AIConversationEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val messages: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
)
