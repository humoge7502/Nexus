package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.usecase.CareerMemoryManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class CareerMemoryMaintenanceWorker @AssistedInject constructor(
@Assisted context: Context,
@Assisted params: WorkerParameters,
private val careerMemoryManager: CareerMemoryManager,
) : CoroutineWorker(context, params) {
override suspend fun doWork(): Result {
return try {
careerMemoryManager.runMaintenance()
Timber.d("CareerMemory weekly maintenance complete")
Result.success()
} catch (e: Exception) {
Timber.e(e, "CareerMemory maintenance failed")
Result.retry()
}
}
}
