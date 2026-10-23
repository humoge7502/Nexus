package com.nexus.domain.repository

import com.nexus.domain.model.LeaderboardEntry

enum class LeaderboardTab(val displayName: String) {
    GLOBAL("Global"),
    COLLEGE("College"),
    FRIENDS("Friends"),
    DOMAIN_DSA("Domain: DSA"),
}

interface ILeaderboardRepository {
    suspend fun getLeaderboard(tab: LeaderboardTab): List<LeaderboardEntry>
    suspend fun getCurrentUserEntry(tab: LeaderboardTab): LeaderboardEntry?
    suspend fun togglePublicVisibility(isPublic: Boolean)
}
