package com.nexus.network.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * GitHub's public REST v3 surface, which — unlike GraphQL — works without
 * a token for public data (60 req/h per IP). This is the path that makes a
 * student's handle useful the moment they paste it, with no OAuth step.
 */
interface GitHubRestService {

    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUserDto

    @GET("users/{username}/repos")
    suspend fun getRepos(
        @Path("username") username: String,
        @Query("sort") sort: String = "pushed",
        @Query("per_page") perPage: Int = 50,
    ): List<GitHubRepoDto>

    @GET("users/{username}/events/public")
    suspend fun getPublicEvents(
        @Path("username") username: String,
        @Query("per_page") perPage: Int = 100,
    ): List<GitHubEventDto>
}

data class GitHubUserDto(
    val login: String? = null,
    val name: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("html_url") val htmlUrl: String? = null,
    @SerializedName("public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class GitHubRepoDto(
    val name: String? = null,
    val description: String? = null,
    @SerializedName("html_url") val htmlUrl: String? = null,
    @SerializedName("stargazers_count") val stargazersCount: Int = 0,
    @SerializedName("forks_count") val forksCount: Int = 0,
    val language: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    val fork: Boolean = false,
)

data class GitHubEventDto(
    val type: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val payload: GitHubEventPayloadDto? = null,
)

data class GitHubEventPayloadDto(
    /** PushEvent only: commits delivered in this push. */
    val size: Int? = null,
)
