package com.nexus.domain.repository

import com.nexus.domain.model.LeaderboardEntry

enum class LeaderboardTab(val displayName: String) {
    GLOBAL("Global"),
    COLLEGE("College"),
    FRIENDS("Friends"),
    DOMAIN_DSA("Domain: DSA"),
}

data class GlobalRanking(val rank: Int, val total: Int)

interface ILeaderboardRepository {
    suspend fun getLeaderboard(tab: LeaderboardTab): List<LeaderboardEntry>
    suspend fun getCurrentUserEntry(tab: LeaderboardTab): LeaderboardEntry?
    suspend fun isPublicProfilePublished(): Boolean

    /** Exact server-side rank among all published profiles, or null when unpublished/unavailable. */
    suspend fun getGlobalRanking(): GlobalRanking?

    suspend fun publishProfile(displayName: String, college: String, totalXp: Int, streakDays: Int)
    suspend fun removePublicProfile()
}
