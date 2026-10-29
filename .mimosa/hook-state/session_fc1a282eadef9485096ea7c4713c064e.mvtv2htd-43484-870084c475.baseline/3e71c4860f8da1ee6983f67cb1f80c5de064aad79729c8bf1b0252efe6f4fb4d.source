package com.nexus.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.nexus.domain.model.LeaderboardEntry
import com.nexus.domain.repository.GlobalRanking
import com.nexus.domain.repository.ILeaderboardRepository
import com.nexus.domain.repository.LeaderboardTab
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : ILeaderboardRepository {

    private val currentUid: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun getLeaderboard(tab: LeaderboardTab): List<LeaderboardEntry> {
        val currentUserId = currentUid ?: return emptyList()
        return try {
            val userDoc = firestore.collection("users").document(currentUserId).get().await()
            val userCollege = userDoc.getString("college") ?: ""
            val friendIds: List<String> =
                (userDoc.get("friendIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val baseQuery = firestore.collection("publicProfiles")
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(100)

            val snapshot = when (tab) {
                LeaderboardTab.GLOBAL -> baseQuery.get().await()
                LeaderboardTab.COLLEGE -> {
                    if (userCollege.isBlank()) return emptyList()
                    baseQuery.whereEqualTo("college", userCollege).get().await()
                }
                LeaderboardTab.FRIENDS -> baseQuery.get().await()
                LeaderboardTab.DOMAIN_DSA -> baseQuery.whereEqualTo("domain", "DSA").get().await()
            }

            snapshot.documents
                .filter { doc ->
                    tab != LeaderboardTab.FRIENDS ||
                        friendIds.contains(doc.id) ||
                        doc.id == currentUserId
                }
                .take(50)
                .mapIndexed { index, doc ->
                    LeaderboardEntry(
                        userId = doc.id,
                        userName = doc.getString("displayName")
                            ?: doc.getString("name")
                            ?: "Anonymous",
                        avatarUrl = doc.getString("avatarUrl"),
                        score = doc.getLong("totalXP")?.toInt() ?: 0,
                        rank = index + 1,
                        isCurrentUser = doc.id == currentUserId,
                        college = doc.getString("college"),
                    )
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load leaderboard from public profiles")
            emptyList()
        }
    }

    override suspend fun getCurrentUserEntry(tab: LeaderboardTab): LeaderboardEntry? =
        getLeaderboard(tab).find { it.isCurrentUser }

    override suspend fun isPublicProfilePublished(): Boolean {
        val userId = currentUid ?: return false
        return try {
            firestore.collection("publicProfiles").document(userId).get().await().exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to read public profile state")
            false
        }
    }

    override suspend fun getGlobalRanking(): GlobalRanking? {
        val userId = currentUid ?: return null
        return try {
            val profiles = firestore.collection("publicProfiles")
            val myXp = profiles.document(userId).get().await().getLong("totalXP")?.toInt()
                ?: return null
            val total = profiles.count().get(AggregateSource.SERVER).await().count.toInt()
            val above = profiles.whereGreaterThan("totalXP", myXp)
                .count().get(AggregateSource.SERVER).await().count.toInt()
            val rank = above + 1
            GlobalRanking(rank = rank, total = maxOf(total, rank))
        } catch (e: Exception) {
            Timber.e(e, "Failed to compute global ranking")
            null
        }
    }

    override suspend fun publishProfile(
        displayName: String,
        college: String,
        totalXp: Int,
        streakDays: Int,
    ) {
        val userId = currentUid ?: error("Sign-in required to publish a public profile")
        firestore.collection("publicProfiles").document(userId)
            .set(
                mapOf(
                    "displayName" to displayName.trim().ifBlank { "Anonymous" },
                    "college" to college.trim(),
                    "totalXP" to totalXp.coerceAtLeast(0),
                    "streak" to streakDays.coerceAtLeast(0),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    override suspend fun removePublicProfile() {
        val userId = currentUid ?: error("Sign-in required to remove a public profile")
        firestore.collection("publicProfiles").document(userId).delete().await()
    }
}
