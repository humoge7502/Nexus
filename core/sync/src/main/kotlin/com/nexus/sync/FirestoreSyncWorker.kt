package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nexus.common.Constants
import com.nexus.common.NetworkMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreSync: FirestoreSync,
    private val networkMonitor: NetworkMonitor,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("FirestoreSyncWorker started")

        return try {
            if (!networkMonitor.isOnline.first()) {
                Timber.d("Offline — skipping Firestore sync")
                return Result.success()
            }

            firestoreSync.syncAll()
            Timber.d("FirestoreSyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "FirestoreSyncWorker failed on attempt $runAttemptCount")
            if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry()
            else Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
        }
    }
}
