package com.nexus.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.nexus.domain.model.StreakData
import com.nexus.domain.repository.IStreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.streakStore by preferencesDataStore(name = "streak_prefs")

@Singleton
class StreakRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : IStreakRepository {
    companion object {
        private val STREAK_COUNT = intPreferencesKey("streak_count")
        private val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        private val LONGEST_STREAK = intPreferencesKey("longest_streak")
        private val STREAK_7_BONUS_AWARDED = booleanPreferencesKey("streak_7_bonus_awarded")
        private val STREAK_30_BONUS_AWARDED = booleanPreferencesKey("streak_30_bonus_awarded")
    }

    override suspend fun checkAndUpdateStreaks() {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val prefs = context.streakStore.data.first()

        val lastActive = prefs[LAST_ACTIVE_DATE] ?: ""
        if (lastActive == today) return

        val currentStreak = prefs[STREAK_COUNT] ?: 0
        val savedLongest = prefs[LONGEST_STREAK] ?: 0

        val newStreak = when {
            lastActive.isEmpty() -> 1
            lastActive == yesterday -> currentStreak + 1
            else -> 1
        }

        val updatedLongest = maxOf(newStreak, savedLongest)

        context.streakStore.edit { editPrefs ->
            editPrefs[STREAK_COUNT] = newStreak
            editPrefs[LONGEST_STREAK] = updatedLongest
            editPrefs[LAST_ACTIVE_DATE] = today
        }
    }

override val currentStreak: Flow<StreakData> = context.streakStore.data.map { prefs ->
val current = prefs[STREAK_COUNT] ?: 0
val savedLongest = prefs[LONGEST_STREAK] ?: 0
val longest = maxOf(current, savedLongest)
StreakData(
currentStreak = current,
longestStreak = longest,
lastActivityDate = prefs[LAST_ACTIVE_DATE] ?: "",
freezeCount = 0,
)
}

    suspend fun recordActivity() = checkAndUpdateStreaks()
}
