package com.nexus.network.api

import com.nexus.network.dto.LeetCodeUserDto
import com.nexus.network.dto.LeetCodeProblemDto
import com.nexus.network.dto.LeetCodeDailyDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeetCodeApiService {
    @GET("{username}")
    suspend fun getUserStats(@Path("username") username: String): Response<LeetCodeUserDto>

    @GET("{username}/solved")
    suspend fun getSolvedProblems(@Path("username") username: String): Response<List<LeetCodeProblemDto>>

    @GET("daily")
    suspend fun getDailyChallenge(): Response<LeetCodeDailyDto>

    @GET("problems")
    suspend fun getAllProblems(
        @Query("limit") limit: Int = 150,
        @Query("skip") skip: Int = 0,
    ): Response<List<LeetCodeProblemDto>>
}
