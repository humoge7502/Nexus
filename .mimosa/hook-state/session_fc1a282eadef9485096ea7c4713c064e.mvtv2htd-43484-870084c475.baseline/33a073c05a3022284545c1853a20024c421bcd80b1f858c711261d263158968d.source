package com.nexus.domain.repository

import com.nexus.domain.model.DailyCareerHit

interface ICareerHitRepository {
    fun getTodaysHit(): DailyCareerHit
    suspend fun isHitViewedToday(): Boolean
    suspend fun markAsViewed()
    suspend fun hasXpBeenAwardedToday(): Boolean
    suspend fun markXpAwarded()
}
