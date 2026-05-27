package com.nexus.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "internships")
data class InternshipEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "company_name") val companyName: String,
    @ColumnInfo(name = "company_logo_url") val companyLogoUrl: String?,
    val location: String,
    @ColumnInfo(name = "is_remote") val isRemote: Boolean,
    @ColumnInfo(name = "stipend_amount") val stipendAmount: Int?,
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

@Entity(tableName = "certifications")
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

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "internship_id") val internshipId: String,
    val status: String = "APPLIED",
    @ColumnInfo(name = "applied_at") val appliedAt: Long = 0,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = 0,
    val notes: String = "",
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    val topic: String = "",
    @ColumnInfo(name = "focus_score") val focusScore: Float = 1f,
)

@Entity(tableName = "achievements")
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

@Entity(tableName = "hackathons")
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

@Entity(tableName = "scholarships")
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

@Entity(tableName = "projects")
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

@Entity(tableName = "goals")
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

@Entity(tableName = "dsa_problems")
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

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "ai_conversations")
data class AIConversationEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val messages: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
)
