package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.domain.repository.CodeforcesProfile
import com.nexus.domain.repository.CodeforcesRatingPoint
import com.nexus.domain.repository.GitHubActivity
import com.nexus.domain.repository.GitHubProfile
import com.nexus.domain.repository.GitHubRepo
import com.nexus.domain.repository.LeetCodeStats
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One façade over the connected developer platforms (GitHub, Codeforces,
 * LeetCode). ViewModels and workers orchestrate a single dependency; a
 * platform that is not connected simply contributes nothing to the snapshot
 * instead of an error the caller must special-case.
 */
data class PlatformSnapshot(
    /** Null when no GitHub handle is connected. */
    val github: GithubSnapshot? = null,
    val leetcode: ApiResult<LeetCodeStats>? = null,
    val codeforces: CodeforcesSnapshot? = null,
)

data class GithubSnapshot(
    val profile: ApiResult<GitHubProfile>,
    val repos: ApiResult<List<GitHubRepo>>,
    val activity: ApiResult<GitHubActivity>,
)

data class CodeforcesSnapshot(
    val profile: ApiResult<CodeforcesProfile>,
    val ratingHistory: ApiResult<List<CodeforcesRatingPoint>>,
)

@Singleton
class PlatformRepository @Inject constructor(
    private val gitHub: GitHubRepository,
    private val codeforces: CodeforcesRepository,
    private val leetCode: LeetCodeRepository,
) {

    /**
     * Fetches every connected platform in parallel. Repositories cache for
     * 10 minutes, so repeat calls inside a session stay off the network and
     * the background platform sync keeps them warm regardless.
     */
    suspend fun snapshot(
        githubHandle: String,
        leetcodeHandle: String,
        codeforcesHandle: String,
    ): PlatformSnapshot = coroutineScope {
        val githubJob = async {
            if (githubHandle.isBlank()) {
                null
            } else {
                GithubSnapshot(
                    profile = gitHub.getProfile(githubHandle),
                    repos = gitHub.getRepositories(githubHandle),
                    activity = gitHub.getRecentActivity(githubHandle),
                )
            }
        }
        val leetcodeJob = async {
            if (leetcodeHandle.isBlank()) {
                null
            } else {
                leetCode.getStats(leetcodeHandle)
            }
        }
        val codeforcesJob = async {
            if (codeforcesHandle.isBlank()) {
                null
            } else {
                CodeforcesSnapshot(
                    profile = codeforces.getProfile(codeforcesHandle),
                    ratingHistory = codeforces.getRatingHistory(codeforcesHandle),
                )
            }
        }
        PlatformSnapshot(
            github = githubJob.await(),
            leetcode = leetcodeJob.await(),
            codeforces = codeforcesJob.await(),
        )
    }
}
