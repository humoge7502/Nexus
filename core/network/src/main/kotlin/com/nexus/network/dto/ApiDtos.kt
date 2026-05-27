package com.nexus.network.dto

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int = 0,
    val totalPages: Int = 1,
    val total: Long = 0,
)

data class InternshipDto(
    val id: String,
    val title: String,
    val company: String,
    @SerializedName("company_logo") val companyLogo: String? = null,
    val location: String = "",
    @SerializedName("is_remote") val isRemote: Boolean = false,
    val stipend: String? = null,
    val duration: String = "",
    val skills: List<String> = emptyList(),
    val deadline: String = "",
    @SerializedName("apply_url") val applyUrl: String = "",
    val description: String = "",
    @SerializedName("posted_at") val postedAt: String = "",
    @SerializedName("is_saved") val isSaved: Boolean = false,
)

data class HackathonDto(
    val id: String,
    val title: String,
    val organizer: String,
    val description: String = "",
    val mode: String = "ONLINE",
    val location: String? = null,
    @SerializedName("start_date") val startDate: String = "",
    @SerializedName("end_date") val endDate: String = "",
    @SerializedName("registration_deadline") val registrationDeadline: String = "",
    @SerializedName("prize_pool") val prizePool: String = "",
    @SerializedName("registration_url") val registrationUrl: String = "",
    @SerializedName("is_live") val isLive: Boolean = false,
    @SerializedName("is_saved") val isSaved: Boolean = false,
    val tags: List<String> = emptyList(),
)

data class GoalDto(
    val id: String = "",
    val title: String,
    val description: String = "",
    val category: String = "SKILL",
    val progress: Float = 0f,
    val target: String = "",
    val deadline: String = "",
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("created_at") val createdAt: String = "",
)

data class DSAProblemDto(
    val id: String,
    val title: String,
    val difficulty: String = "MEDIUM",
    val platform: String = "LeetCode",
    val url: String = "",
    val topics: List<String> = emptyList(),
    @SerializedName("is_solved") val isSolved: Boolean = false,
)

data class AchievementDto(
    val id: String,
    val title: String,
    val description: String = "",
    val icon: String = "",
    val category: String = "GENERAL",
    @SerializedName("unlocked_at") val unlockedAt: String? = null,
    val progress: Float = 0f,
)

data class LeaderboardEntryDto(
    val rank: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("display_name") val displayName: String,
    val score: Int,
    val avatar: String? = null,
)

data class ChatMessageDto(
    val id: String = "",
    val role: String,
    val content: String,
    val timestamp: String = "",
)

data class ChatRequestDto(
    @SerializedName("conversation_id") val conversationId: String = "",
    val message: String,
)

data class ChatResponseDto(
    val reply: String,
    @SerializedName("conversation_id") val conversationId: String = "",
)

data class ResumeRequestDto(
    val content: String,
    @SerializedName("job_description") val jobDescription: String? = null,
)

data class ResumeAnalysisDto(
    @SerializedName("ats_score") val atsScore: Int = 0,
    @SerializedName("missing_keywords") val missingKeywords: List<String> = emptyList(),
    @SerializedName("section_scores") val sectionScores: Map<String, Int> = emptyMap(),
    val recommendations: List<String> = emptyList(),
    val summary: String = "",
)

data class UserProfileDto(
    val id: String = "",
    @SerializedName("display_name") val displayName: String = "",
    val email: String = "",
    val avatar: String? = null,
    val university: String = "",
    val year: Int = 0,
    val skills: List<String> = emptyList(),
    @SerializedName("career_score") val careerScore: Int = 0,
    val streak: Int = 0,
)
