package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.common.Constants
import com.nexus.common.NetworkMonitor
import com.nexus.common.SafeApiCall
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.GoalRepository
import com.nexus.data.repository.HackathonRepository
import com.nexus.data.repository.InternshipRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val internshipRepository: InternshipRepository,
    private val certificationRepository: CertificationRepository,
    private val hackathonRepository: HackathonRepository,
    private val goalRepository: GoalRepository,
    private val dsaProblemRepository: DSAProblemRepository,
    private val networkMonitor: NetworkMonitor,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val hasNetwork = networkMonitor.isOnline.first()
        if (!hasNetwork) {
            Timber.d("SyncWorker: offline, rescheduling")
            return Result.retry()
        }
        return try {
            val results = mapOf(
                "internships" to SafeApiCall.run("sync_internships") { internshipRepository.refreshInternships() },
                "certifications" to SafeApiCall.run("sync_certifications") { certificationRepository.refreshCertifications() },
                "hackathons" to SafeApiCall.run("sync_hackathons") { hackathonRepository.refreshHackathons() },
                "goals" to SafeApiCall.run("sync_goals") { goalRepository.refreshGoals() },
                "dsa_problems" to SafeApiCall.run("sync_dsa") { dsaProblemRepository.refreshProblems() },
            )

            val failures = results.filterValues { it.isError }
            if (failures.isEmpty()) {
                Timber.d("Background sync completed successfully")
                Result.success()
            } else {
                Timber.w("Background sync had ${failures.size} failures: ${failures.keys}")
                if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Background sync failed catastrophically")
            if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
