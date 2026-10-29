package com.nexus.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.rateLimitStore by preferencesDataStore(name = "ai_rate_limit")

sealed interface RateLimitResult {
    data class Allowed(val remaining: Int) : RateLimitResult
    data class LimitExceeded(val limit: Int, val isPro: Boolean, val callsToday: Int = limit) : RateLimitResult
}

@Singleton
class AIRateLimiter private constructor(
    private val context: Context?,
    private val testDataStore: DataStore<Preferences>?,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context, null)

    constructor(dataStore: DataStore<Preferences>) : this(null, dataStore)

    private val dataStore: DataStore<Preferences>
        get() = testDataStore ?: requireNotNull(context).rateLimitStore

    companion object {
        private val CALLS_TODAY = intPreferencesKey("ai_calls_today")
        private val RESET_DAY = longPreferencesKey("ai_calls_reset_day")
        const val FREE_DAILY_LIMIT = 10
    }

    suspend fun canMakeCall(isPro: Boolean): RateLimitResult {
        if (isPro) return RateLimitResult.Allowed(Int.MAX_VALUE)

        val today = System.currentTimeMillis() / 86_400_000L
        val prefs = dataStore.data.first()
        val resetDay = prefs[RESET_DAY] ?: 0L
        val calls = if (resetDay < today) {
            dataStore.edit { it[CALLS_TODAY] = 0; it[RESET_DAY] = today }
            0
        } else {
            prefs[CALLS_TODAY] ?: 0
        }

        return if (calls >= FREE_DAILY_LIMIT) {
            RateLimitResult.LimitExceeded(limit = FREE_DAILY_LIMIT, isPro = false, callsToday = calls)
        } else {
            RateLimitResult.Allowed(remaining = FREE_DAILY_LIMIT - calls)
        }
    }

    suspend fun canMakeAICall(isPro: Boolean): RateLimitResult = canMakeCall(isPro)

    suspend fun recordCall() {
        val today = System.currentTimeMillis() / 86_400_000L
        dataStore.edit { prefs ->
            val resetDay = prefs[RESET_DAY] ?: today
            val current = if (resetDay < today) 0 else (prefs[CALLS_TODAY] ?: 0)
            prefs[RESET_DAY] = today
            prefs[CALLS_TODAY] = current + 1
        }
    }

    suspend fun recordAICall() = recordCall()
}
