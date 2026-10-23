package com.nexus.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.ScoreSnapshotDao
import com.nexus.database.entity.ScoreSnapshotEntity
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.repository.IUserXpRepository
import com.nexus.domain.repository.XPAwardResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import timber.log.Timber

@Singleton
class UserXpRepository @Inject constructor(
    private val userPreferences: UserPreferences,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val analyticsTracker: AnalyticsTracker,
    private val scoreSnapshotDao: ScoreSnapshotDao,
) : IUserXpRepository {
    private val totalXpKey = intPreferencesKey("total_xp")
    private val levelKey = intPreferencesKey("user_level")
    private val xpTodayKey = intPreferencesKey("xp_today")
    private val xpTodayDateKey = stringPreferencesKey("xp_today_date")

    override val totalXP: Flow<Int> = userPreferences.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[totalXpKey] ?: 0 }

    override val currentLevel: Flow<Int> = totalXP.map(::computeLevel)

    val xpToNextLevel: Flow<Int> = totalXP.map { xp ->
        (nextLevelThreshold(computeLevel(xp)) - xp).coerceAtLeast(0)
    }

    val xpEarnedToday: Flow<Int> = userPreferences.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val today = LocalDate.now().toString()
            if (prefs[xpTodayDateKey] == today) prefs[xpTodayKey] ?: 0 else 0
        }

    override suspend fun awardXP(amount: Int, source: String): XPAwardResult {
        var previousXP = 0
        var newXP = 0
        var newLevel = 1
        var leveledUp = false

        userPreferences.dataStore.edit { prefs ->
            previousXP = prefs[totalXpKey] ?: 0
            newXP = (previousXP + amount).coerceAtLeast(0)
            prefs[totalXpKey] = newXP

            val today = LocalDate.now().toString()
            if (prefs[xpTodayDateKey] == today) {
                prefs[xpTodayKey] = (prefs[xpTodayKey] ?: 0) + amount
            } else {
                prefs[xpTodayDateKey] = today
                prefs[xpTodayKey] = amount
            }

            newLevel = computeLevel(newXP)
            leveledUp = newLevel > computeLevel(previousXP)
            prefs[levelKey] = newLevel
        }

        analyticsTracker.track("xp_awarded", mapOf(
            "amount" to amount,
            "source" to source,
            "total_xp" to newXP,
            "level" to newLevel,
        ))
        if (leveledUp) {
            analyticsTracker.track("level_up", mapOf("level" to newLevel, "total_xp" to newXP))
        }

        syncXPToFirestore(newXP, newLevel)
        return XPAwardResult(amount, newXP, leveledUp, newLevel, source)
    }

    fun computeLevel(xp: Int): Int = when {
        xp <= 500 -> 1
        xp <= 1500 -> 2
        xp <= 4000 -> 3
        xp <= 10000 -> 4
        xp <= 25000 -> 5
        else -> 6
    }

    fun levelName(level: Int): String = when (level) {
        1 -> "Rookie"
        2 -> "Builder"
        3 -> "Engineer"
        4 -> "Architect"
        5 -> "Pioneer"
        6 -> "NEXUS Legend"
        else -> "Rookie"
    }

    private fun nextLevelThreshold(level: Int): Int = when (level) {
        1 -> 501
        2 -> 1501
        3 -> 4001
        4 -> 10001
        5 -> 25001
        else -> Int.MAX_VALUE
    }

private fun syncXPToFirestore(xp: Int, level: Int) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update(mapOf("totalXP" to xp, "level" to level, "lastXPSync" to System.currentTimeMillis()))
            .addOnFailureListener { Timber.e(it, "Failed to sync XP to Firestore") }
    }
    
    override fun getSnapshotsForUser(): Flow<List<ScoreSnapshot>> {
        val userId = firebaseAuth.currentUser?.uid ?: return flowOf(emptyList())
        return scoreSnapshotDao.getHistoryForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

private fun ScoreSnapshotEntity.toDomain() = ScoreSnapshot(
    id = id, userId = userId, score = score, componentsJson = components,
    delta = delta, recordedAt = recordedAt
)
