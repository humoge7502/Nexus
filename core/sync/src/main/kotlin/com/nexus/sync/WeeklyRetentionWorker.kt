package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.repository.StreakRepository
import com.nexus.data.usecase.RetentionTracker
import com.nexus.database.dao.AIDao
import com.nexus.database.dao.ApplicationDao
import com.nexus.database.dao.GoalDao
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.dao.ProjectDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Weekly cohort retention row for the business metrics in docs/BUSINESS_MODEL.md.
 * Every field is derived from rows the student actually produced: a surface is
 * only listed when it holds their data, and the count is the real streak length,
 * capped at the seven days the row covers.
 */
@HiltWorker
class WeeklyRetentionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val retentionTracker: RetentionTracker,
    private val streakRepository: StreakRepository,
    private val featureReader: RetentionFeatureReader,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val activeDays = streakRepository.currentStreak.first().currentStreak.coerceIn(0, 7)
        retentionTracker.recordWeeklyActivity(activeDays, usedFeatures())
        Timber.d("Weekly retention recorded ($activeDays active days)")
        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "WeeklyRetentionWorker failed")
        if (runAttemptCount < 2) Result.retry() else Result.failure()
    }

    private suspend fun usedFeatures(): List<String> = featureReader.usedFeatures()

    companion object {
        const val WORK_NAME = "weekly_retention"
    }
}

/**
 * Which surfaces of the app hold this student's data.
 *
 * Kept out of the worker so the weekly row stays a scheduling concern and the
 * DAO fan-out stays a data question; a feature is only listed when a real row
 * exists, never inferred from the fact that a screen was opened.
 */
class RetentionFeatureReader @Inject constructor(
    private val internshipDao: InternshipDao,
    private val hackathonDao: HackathonDao,
    private val applicationDao: ApplicationDao,
    private val projectDao: ProjectDao,
    private val goalDao: GoalDao,
    private val aiDao: AIDao,
) {
    suspend fun usedFeatures(): List<String> = buildList {
        if (internshipDao.getSavedIds().isNotEmpty() || hackathonDao.getSavedIds().isNotEmpty()) {
            add("opportunities")
        }
        if (applicationDao.getAllEntities().isNotEmpty()) add("applications")
        if (projectDao.getAll().first().isNotEmpty()) add("portfolio")
        if (goalDao.getAllEntities().isNotEmpty()) add("goals")
        if (aiDao.getAllEntities().isNotEmpty()) add("ai")
    }
}
