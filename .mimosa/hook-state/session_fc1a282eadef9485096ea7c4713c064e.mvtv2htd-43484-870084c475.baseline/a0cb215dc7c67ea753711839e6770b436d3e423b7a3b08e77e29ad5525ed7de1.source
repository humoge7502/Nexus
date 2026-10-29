package com.nexus.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexus.domain.model.DailyCareerHit
import com.nexus.domain.repository.ICareerHitRepository
import com.nexus.domain.usecase.DailyCareerHitGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.careerHitStore by preferencesDataStore(name = "career_hit_prefs")

@Singleton
class CareerHitRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generator: DailyCareerHitGenerator,
) : ICareerHitRepository {
    companion object {
        private val TODAY_HIT_ID = stringPreferencesKey("today_hit_id")
        private val HIT_VIEWED_DATE = stringPreferencesKey("hit_viewed_date")
        private val LAST_HIT_XP_DATE = stringPreferencesKey("last_hit_xp_date")
    }

    override fun getTodaysHit(): DailyCareerHit {
        return generator.getHitForDate()
    }

    suspend fun getStoredTodaysHitId(): String? {
        return context.careerHitStore.data.map { prefs ->
            prefs[TODAY_HIT_ID]
        }.first()
    }

    suspend fun setTodaysHitId(id: String) {
        context.careerHitStore.edit { prefs ->
            prefs[TODAY_HIT_ID] = id
        }
    }

    override suspend fun isHitViewedToday(): Boolean {
        val today = LocalDate.now().toString()
        return context.careerHitStore.data.map { prefs ->
            prefs[HIT_VIEWED_DATE] == today
        }.first()
    }

    override suspend fun markAsViewed() {
        val today = LocalDate.now().toString()
        context.careerHitStore.edit { prefs ->
            prefs[HIT_VIEWED_DATE] = today
        }
    }

    override suspend fun hasXpBeenAwardedToday(): Boolean {
        val today = LocalDate.now().toString()
        return context.careerHitStore.data.map { prefs ->
            prefs[LAST_HIT_XP_DATE] == today
        }.first()
    }

    override suspend fun markXpAwarded() {
        val today = LocalDate.now().toString()
        context.careerHitStore.edit { prefs ->
            prefs[LAST_HIT_XP_DATE] = today
        }
    }
}
