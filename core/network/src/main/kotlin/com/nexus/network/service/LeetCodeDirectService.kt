package com.nexus.network.service

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Direct LeetCode public GraphQL. Primary path remains our Cloud Function
 * (it caches in Firestore and keeps distributed APK traffic off
 * leetcode.com); this client exists because the backend is not deployed
 * until Firebase Blaze, and a student who connects their handle today
 * should still see their real solved counts.
 */
interface LeetCodeDirectService {

    @POST("graphql")
    suspend fun query(@Body request: LeetCodeGraphQLRequest): LeetCodeGraphQLResponse
}

data class LeetCodeGraphQLRequest(
    val query: String,
    val variables: Map<String, String> = emptyMap(),
)

data class LeetCodeGraphQLResponse(
    val data: LeetCodeGraphQLData? = null,
    val errors: List<LeetCodeGraphQLErrorDto>? = null,
) {
    val hasErrors: Boolean get() = !errors.isNullOrEmpty()
}

data class LeetCodeGraphQLErrorDto(val message: String? = null)

data class LeetCodeGraphQLData(val matchedUser: LeetCodeMatchedUserDto? = null)

data class LeetCodeMatchedUserDto(
    val username: String? = null,
    val profile: LeetCodeProfileDto? = null,
    val submitStats: LeetCodeSubmitStatsDto? = null,
)

data class LeetCodeProfileDto(
    val ranking: Int? = null,
    val reputation: Int? = null,
)

data class LeetCodeSubmitStatsDto(
    /** One entry per difficulty plus an "All" rollup. */
    val acSubmissionNum: List<LeetCodeAcCountDto>? = null,
)

data class LeetCodeAcCountDto(
    val difficulty: String? = null,
    val count: Int? = null,
)
