package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.common.ApiResult
import com.nexus.common.Constants
import com.nexus.data.repository.PlatformRepository
import com.nexus.datastore.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Warms the connected-platform caches (GitHub, Codeforces, LeetCode) on a
 * rhythm, so the Profile screen is instant, stays meaningful offline, and the
 * student's real coding activity shows up in the app without them opening
 * anything. This is the automation layer for the accounts connected in
 * Settings — no manual refresh step anywhere in the UI.
 */
@HiltWorker
class PlatformSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val platformRepository: PlatformRepository,
    private val userPreferences: UserPreferences,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val github = userPreferences.githubUsername.first().trim()
        val codeforces = userPreferences.codeforcesUsername.first().trim()
        val leetcode = userPreferences.leetcodeUsername.first().trim()

        if (github.isBlank() && codeforces.isBlank() && leetcode.isBlank()) {
            Timber.d("No platform handles connected; skipping platform sync")
            return Result.success()
        }

        val snap = platformRepository.snapshot(
            githubHandle = github,
            leetcodeHandle = leetcode,
            codeforcesHandle = codeforces,
        )
        val results = listOfNotNull(
            snap.github?.profile,
            snap.github?.repos,
            snap.github?.activity,
            snap.leetcode,
            snap.codeforces?.profile,
            snap.codeforces?.ratingHistory,
        )

        val warmed = results.count { it is ApiResult.Success }
        val attempted = results.size
        Timber.d("Platform sync warmed $warmed/$attempted platform fetches")

        return when {
            // A single platform being unreachable must not fail the whole
            // pass; only an all-red run is worth retrying.
            results.any { it is ApiResult.Success } -> Result.success()
            attempted == 0 -> Result.success()
            runAttemptCount < Constants.MAX_RETRY_ATTEMPTS -> Result.retry()
            else -> Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "platform_sync"
    }
}
