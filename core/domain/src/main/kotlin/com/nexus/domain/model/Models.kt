package com.nexus.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant
import java.time.LocalDate

data class Internship(
    val id: String,
    val title: String,
    val company: Company,
    val location: Location,
    val stipend: Stipend?,
    val duration: Duration,
    val requiredSkills: ImmutableList<Skill> = persistentListOf(),
    val deadline: LocalDate,
    val applyUrl: String,
    val description: String = "",
    val isRemote: Boolean = false,
    val isSaved: Boolean = false,
    val relevanceScore: Float = 0f,
    val postedAt: Instant,
    val tags: ImmutableList<String> = persistentListOf(),
)

data class Company(
    val name: String,
    val logoUrl: String? = null,
    val domain: String = "",
    val websiteUrl: String? = null,
)

data class Location(
    val city: String,
    val state: String = "",
    val country: String = "India",
)

data class Stipend(
    val amount: Int,
    val currency: String = "INR",
    val period: StipendPeriod = StipendPeriod.MONTHLY,
) {
    fun formatted(): String = "₹$amount/${period.label}"
}

enum class StipendPeriod(val label: String) {
    MONTHLY("mo"), ONE_TIME("once"), NEGOTIATED("negotiable")
}

data class Duration(
    val length: Int,
    val unit: DurationUnit = DurationUnit.MONTHS,
) {
    fun formatted(): String = "$length ${unit.label}"
}

enum class DurationUnit(val label: String) {
    MONTHS("months"), WEEKS("weeks"), DAYS("days")
}

data class Skill(
    val id: String = "",
    val name: String,
    val category: SkillCategory = SkillCategory.GENERAL,
    val iconUrl: String? = null,
)

enum class SkillCategory {
    MOBILE_DEVELOPMENT, WEB_DEVELOPMENT, DATA_SCIENCE, AI_ML,
    CLOUD_COMPUTING, CYBERSECURITY, DEVOPS, BLOCKCHAIN, UI_UX,
    PROGRAMMING, DATABASE, GENERAL
}

enum class ApplicationStatus {
    NOT_APPLIED, APPLIED, IN_REVIEW, INTERVIEW, ACCEPTED, REJECTED, COMPLETED
}

data class Certification(
    val id: String,
    val title: String,
    val provider: CertProvider,
    val category: SkillCategory,
    val difficulty: DifficultyLevel,
    val estimatedHours: Int,
    val skills: ImmutableList<Skill> = persistentListOf(),
    val linkedInBadge: LinkedInBadge? = null,
    val price: Price = Price.Free,
    val rating: Float = 0f,
    val enrollmentUrl: String = "",
    val isTrending: Boolean = false,
    val completionRate: Float = 0f,
    val employerRecognition: Int = 0,
    val userProgress: CertProgress? = null,
)

data class CertProgress(
    val certificationId: String,
    val enrolledAt: Instant? = null,
    val targetCompletionDate: LocalDate? = null,
    val completedModules: Int = 0,
    val totalModules: Int = 0,
    val studyHoursLogged: Int = 0,
    val completedAt: Instant? = null,
    val certificateUrl: String? = null,
) {
    val progressPercent: Float get() =
        if (totalModules == 0) 0f else completedModules.toFloat() / totalModules
    val isCompleted: Boolean get() = completedAt != null
}

enum class CertProvider(val displayName: String) {
    GOOGLE("Google"), META("Meta"), AWS("Amazon Web Services"),
    MICROSOFT("Microsoft"), IBM("IBM"), COURSERA("Coursera"),
    UDEMY("Udemy"), LINKEDIN("LinkedIn Learning"), OTHER("Other")
}

enum class DifficultyLevel { BEGINNER, INTERMEDIATE, ADVANCED }

data class LinkedInBadge(
    val name: String,
    val issuer: String,
    val imageUrl: String? = null,
    val badgeUrl: String? = null,
)

sealed interface Price {
    data object Free : Price
    data class Paid(val amount: Int, val currency: String = "USD") : Price
}

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val academicYear: String = "",
    val college: String = "",
    val skills: ImmutableList<Skill> = persistentListOf(),
    val interests: ImmutableList<String> = persistentListOf(),
    val careerGoal: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completedCerts: Int = 0,
    val totalApplications: Int = 0,
) {
    val displayName: String get() = "$firstName $lastName"
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val iconUrl: String? = null,
    val isUnlocked: Boolean = false,
    val unlockedAt: Instant? = null,
    val progress: Float = 0f,
    val maxProgress: Float = 1f,
    val xpReward: Int = 0,
)

enum class AchievementType {
    FIRST_INTERNSHIP, FIRST_CERTIFICATION, STREAK_7, STREAK_30,
    TEN_APPLICATIONS, FIVE_CERTS, MASTER_CODER, NETWORKER,
    EARLY_BIRD, NIGHT_OWL
}

data class StudySession(
    val id: String,
    val startTime: Instant,
    val durationMinutes: Int,
    val topic: String = "",
    val focusScore: Float = 1f,
)

data class DailyQuote(
    val text: String,
    val author: String,
)

data class UserStats(
    val totalApplications: Int = 0,
    val completedCerts: Int = 0,
    val currentStreak: Int = 0,
    val interviewsScheduled: Int = 0,
    val studyMinutesToday: Int = 0,
    val careerReadinessScore: Float = 0f,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

// NEW: Hackathon model
data class Hackathon(
    val id: String,
    val title: String,
    val organizer: String,
    val description: String = "",
    val mode: EventMode = EventMode.ONLINE,
    val location: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val registrationDeadline: LocalDate,
    val prizePool: String = "",
    val teamSize: IntRange = 1..4,
    val skills: ImmutableList<Skill> = persistentListOf(),
    val tags: ImmutableList<String> = persistentListOf(),
    val registrationUrl: String = "",
    val isLive: Boolean = false,
    val isSaved: Boolean = false,
    val relevanceScore: Float = 0f,
)

enum class EventMode { ONLINE, OFFLINE, HYBRID }

// NEW: Scholarship model
data class Scholarship(
    val id: String,
    val title: String,
    val provider: String,
    val amount: String,
    val description: String = "",
    val eligibility: String = "",
    val deadline: LocalDate,
    val applicationUrl: String = "",
    val isOpen: Boolean = true,
    val tags: ImmutableList<String> = persistentListOf(),
)

// NEW: OpenSourceProgram model
data class OpenSourceProgram(
    val id: String,
    val title: String,
    val organization: String,
    val description: String = "",
    val applicationDeadline: LocalDate,
    val programDuration: String = "",
    val stipend: String = "",
    val isPaid: Boolean = true,
    val applicationUrl: String = "",
    val skills: ImmutableList<Skill> = persistentListOf(),
    val isSaved: Boolean = false,
)

// NEW: Workshop model
data class Workshop(
    val id: String,
    val title: String,
    val provider: String,
    val description: String = "",
    val dateTime: LocalDate? = null,
    val duration: String = "",
    val mode: EventMode = EventMode.ONLINE,
    val registrationUrl: String = "",
    val isFree: Boolean = true,
    val price: String = "",
    val tags: ImmutableList<String> = persistentListOf(),
)

// NEW: ResearchOpportunity model
data class ResearchOpportunity(
    val id: String,
    val title: String,
    val institution: String,
    val field: String = "",
    val description: String = "",
    val deadline: LocalDate,
    val duration: String = "",
    val stipend: String = "",
    val applicationUrl: String = "",
    val professorName: String? = null,
    val isRemote: Boolean = true,
)

// NEW: Project model
data class PortfolioProject(
    val id: String,
    val title: String,
    val description: String = "",
    val techStack: ImmutableList<String> = persistentListOf(),
    val githubUrl: String? = null,
    val liveUrl: String? = null,
    val imageUrl: String? = null,
    val starCount: Int = 0,
    val isFeatured: Boolean = false,
    val completedAt: LocalDate? = null,
    val category: SkillCategory = SkillCategory.GENERAL,
)

// NEW: Goal model
data class Goal(
    val id: String,
    val title: String,
    val description: String = "",
    val category: GoalCategory,
    val progress: Float = 0f,
    val targetDate: LocalDate? = null,
    val milestones: ImmutableList<Milestone> = persistentListOf(),
    val isCompleted: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
)

enum class GoalCategory {
    CERTIFICATION, INTERNSHIP, DSA, PROJECT, SKILL, HACKATHON, PLACEMENT, OTHER
}

data class Milestone(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null,
)

// NEW: DSAProblem model
data class DSAProblem(
    val id: String,
    val title: String,
    val difficulty: DsaDifficulty,
    val category: String = "",
    val platform: String = "LeetCode",
    val link: String = "",
    val topics: ImmutableList<String> = persistentListOf(),
    val frequency: Float = 0f,
    val status: DsaStatus = DsaStatus.NOT_ATTEMPTED,
    val solvedAt: Instant? = null,
    val userNotes: String = "",
)

enum class DsaDifficulty { EASY, MEDIUM, HARD }
enum class DsaStatus { NOT_ATTEMPTED, ATTEMPTED, SOLVED, REVISIT }

// NEW: Gamification models
data class UserLevel(
    val level: Int = 1,
    val title: String = "Rookie",
    val currentXp: Int = 0,
    val xpForNextLevel: Int = 500,
)

data class LeaderboardEntry(
    val userId: String,
    val userName: String,
    val avatarUrl: String? = null,
    val score: Int = 0,
    val rank: Int = 0,
    val isCurrentUser: Boolean = false,
)

data class NexusScore(
    val overallScore: Int = 0,
    val skillProgress: Float = 0f,
    val opportunityEngagement: Float = 0f,
    val consistency: Float = 0f,
    val portfolioStrength: Float = 0f,
    val communityContribution: Float = 0f,
    val percentile: Int = 0,
    val weeklyChange: Int = 0,
)

// NEW: Notification model
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val createdAt: Instant = Instant.now(),
)

enum class NotificationType {
    OPPORTUNITY_ALERT, DEADLINE_REMINDER, STREAK_REMINDER,
    ACHIEVEMENT_UNLOCKED, AI_INSIGHT, LEVEL_UP, WEEKLY_REPORT, SYSTEM
}

// NEW: AI models
data class AIConversation(
    val id: String,
    val title: String = "",
    val messages: ImmutableList<AIMessage> = persistentListOf(),
    val createdAt: Instant = Instant.now(),
    val lastMessageAt: Instant = Instant.now(),
)

data class AIMessage(
    val id: String,
    val role: AiMessageRole,
    val content: String,
    val timestamp: Instant = Instant.now(),
)

enum class AiMessageRole { USER, ASSISTANT, SYSTEM }

// NEW: ResumeAnalysis model
data class ResumeAnalysis(
    val atsScore: Int = 0,
    val sectionScores: Map<String, SectionScore> = emptyMap(),
    val missingKeywords: ImmutableList<String> = persistentListOf(),
    val recommendations: ImmutableList<String> = persistentListOf(),
)

data class SectionScore(
    val score: Int,
    val status: SectionStatus,
    val message: String = "",
)

enum class SectionStatus { EXCELLENT, GOOD, NEEDS_WORK, MISSING }

// NEW: SkillRoadmap model
data class SkillRoadmap(
    val id: String,
    val targetRole: String = "",
    val estimatedDuration: String = "",
    val phases: ImmutableList<RoadmapPhase> = persistentListOf(),
    val isAIGenerated: Boolean = false,
)

data class RoadmapPhase(
    val id: String,
    val title: String,
    val duration: String = "",
    val items: ImmutableList<RoadmapItem> = persistentListOf(),
)

data class RoadmapItem(
    val id: String,
    val title: String,
    val type: RoadmapItemType,
    val resourceUrl: String? = null,
    val estimatedHours: Int = 0,
    val isCompleted: Boolean = false,
)

enum class RoadmapItemType { COURSE, PROJECT, CERTIFICATION, PRACTICE, BOOK, ARTICLE }

// NEW: SkillGap model
data class SkillGapAnalysis(
    val userSkills: ImmutableList<Skill> = persistentListOf(),
    val targetSkills: ImmutableList<Skill> = persistentListOf(),
    val gaps: ImmutableList<SkillGap> = persistentListOf(),
)

data class SkillGap(
    val skill: Skill,
    val priority: GapPriority,
    val reason: String = "",
    val recommendedResources: ImmutableList<String> = persistentListOf(),
)

enum class GapPriority { CRITICAL, RECOMMENDED, OPTIONAL }
