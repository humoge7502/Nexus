package com.nexus.domain.repository

import com.nexus.domain.model.ScoreSnapshot
import kotlinx.coroutines.flow.Flow

interface IUserXpRepository {
  val totalXP: Flow<Int>
  val currentLevel: Flow<Int>
  suspend fun awardXP(amount: Int, source: String): XPAwardResult
  fun getSnapshotsForUser(): Flow<List<ScoreSnapshot>>
}

data class XPAwardResult(
    val xpAwarded: Int,
    val totalXP: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
    val source: String
)
