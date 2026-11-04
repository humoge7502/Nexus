package com.nexus.domain.repository

import com.nexus.common.ApiResult

data class GitHubContributions(
    val totalContributions: Int,
    val weeks: List<List<DayData>>,
)

data class DayData(
    val count: Int,
    val date: String,
    val color: String,
)

data class GitHubRepo(
    val name: String,
    val description: String,
    val url: String,
    val stars: Int,
    val forks: Int,
    val primaryLanguage: String?,
    val updatedAt: String,
    val isFork: Boolean,
)

data class GitHubProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val profileUrl: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
)

/** One day of observed public events (GitHub REST events feed, UTC dates). */
data class ActivityDay(
    /** ISO-8601 calendar date (YYYY-MM-DD, UTC). */
    val date: String,
    val events: Int,
    val commits: Int,
)

data class GitHubActivity(
    val totalEvents: Int,
    val totalCommits: Int,
    /** Consecutive active days ending today or yesterday, UTC. */
    val currentStreakDays: Int,
    /** Ascending by date; covers only the window the feed actually returned. */
    val days: List<ActivityDay>,
)

interface IGitHubRepository {
    suspend fun getProfile(username: String): ApiResult<GitHubProfile>
    suspend fun getRepositories(username: String): ApiResult<List<GitHubRepo>>
    suspend fun getRecentActivity(username: String): ApiResult<GitHubActivity>

    /**
     * Full contribution calendar — GitHub GraphQL, which requires a personal
     * access token. With no token connected this returns Error; use
     * [getRecentActivity] for the tokenless path.
     */
    suspend fun getContributions(username: String): ApiResult<GitHubContributions>
}
