package com.nexus.domain.repository

import com.nexus.domain.model.StreakData
import kotlinx.coroutines.flow.Flow

interface IStreakRepository {
  val currentStreak: Flow<StreakData>
  suspend fun checkAndUpdateStreaks()
}
