package com.nexus.network.api

import com.nexus.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NexusApiService {

    @GET("internships")
    suspend fun getInternships(
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("remote") isRemote: Boolean? = null,
        @Query("minStipend") minStipend: Int? = null,
        @Query("skills") skills: String? = null,
    ): Response<PaginatedResponse<InternshipDto>>

    @GET("internships/{id}")
    suspend fun getInternshipById(@Path("id") id: String): Response<InternshipDto>

    @GET("career-hits/daily")
    suspend fun getDailyCareerHits(): Response<List<com.nexus.network.dto.CareerHitDto>>

    @GET("skills/graph/nodes")
    suspend fun getSkillGraphNodes(): Response<List<com.nexus.network.dto.SkillNodeDto>>

    @GET("skills/graph/edges")
    suspend fun getSkillGraphEdges(): Response<List<com.nexus.network.dto.SkillEdgeDto>>

    @GET("config/embedding")
    suspend fun getEmbeddingConfig(): Response<com.nexus.network.dto.EmbeddingConfigDto>

    @GET("certifications/recommended")
    suspend fun getRecommendedCertifications(
        @Query("limit") limit: Int = 10,
    ): Response<List<CertificationDto>>

    @GET("hackathons")
    suspend fun getHackathons(
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
    ): Response<PaginatedResponse<HackathonDto>>

    @GET("hackathons/{id}")
    suspend fun getHackathonById(@Path("id") id: String): Response<HackathonDto>

    @GET("goals")
    suspend fun getGoals(): Response<List<GoalDto>>

    @POST("goals")
    suspend fun createGoal(@Body goal: GoalDto): Response<GoalDto>

    @PUT("goals/{id}")
    suspend fun updateGoal(@Path("id") id: String, @Body goal: GoalDto): Response<GoalDto>

    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") id: String): Response<Unit>

    @GET("dsa/problems")
    suspend fun getDSAProblems(
        @Query("difficulty") difficulty: String? = null,
    ): Response<List<DSAProblemDto>>

    @PUT("dsa/problems/{id}")
    suspend fun toggleDSAProblem(@Path("id") id: String, @Body body: Map<String, Boolean>): Response<DSAProblemDto>

    @GET("achievements")
    suspend fun getAchievements(): Response<List<AchievementDto>>

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("limit") limit: Int = 20,
    ): Response<List<LeaderboardEntryDto>>

    @GET("ai/chat/{conversationId}")
    suspend fun getChatHistory(@Path("conversationId") id: String): Response<List<ChatMessageDto>>

    @POST("ai/chat")
    suspend fun sendChatMessage(@Body message: ChatRequestDto): Response<ChatResponseDto>

    @POST("ai/resume/analyze")
    suspend fun analyzeResume(@Body resume: ResumeRequestDto): Response<ResumeAnalysisDto>

    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("users/profile")
    suspend fun updateProfile(@Body profile: UserProfileDto): Response<UserProfileDto>
}
