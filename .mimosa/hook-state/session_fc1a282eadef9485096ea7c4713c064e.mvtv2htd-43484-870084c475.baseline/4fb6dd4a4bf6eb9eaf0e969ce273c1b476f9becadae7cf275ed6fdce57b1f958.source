package com.nexus.domain.repository

import com.nexus.common.NexusResult
import kotlinx.coroutines.flow.Flow

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

interface IGitHubRepository {
    fun getContributions(username: String): Flow<NexusResult<GitHubContributions>>
    fun getRepositories(username: String): Flow<NexusResult<List<GitHubRepo>>>
}
