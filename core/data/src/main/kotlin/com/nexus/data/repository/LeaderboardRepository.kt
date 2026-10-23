package com.nexus.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexus.domain.model.LeaderboardEntry
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
    override suspend fun getLeaderboard(tab: LeaderboardTab): List<LeaderboardEntry> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return emptyList()
            val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
            val userCollege = currentUserDoc.getString("college") ?: ""
            val friendIds: List<String> = (currentUserDoc.get("friendIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val baseQuery = firestore.collection("users")
                .whereEqualTo("isLeaderboardPublic", true)
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(100)

            val snapshot = when (tab) {
                LeaderboardTab.GLOBAL -> baseQuery.get().await()
                LeaderboardTab.COLLEGE -> {
                    if (userCollege.isBlank()) return emptyList()
                    baseQuery.whereEqualTo("college", userCollege).get().await()
                }
                LeaderboardTab.FRIENDS -> baseQuery.get().await()
                LeaderboardTab.DOMAIN_DSA -> {
                    baseQuery.whereEqualTo("domain", "DSA").get().await()
                }
            }

            val domainDsaFilter: (Map<String?, Any?>) -> Boolean = { data ->
                tab != LeaderboardTab.DOMAIN_DSA || data["domain"]?.toString()?.equals("DSA", ignoreCase = true) == true
            }

            val collegeFilter: (Map<String?, Any?>) -> Boolean = { data ->
                tab != LeaderboardTab.COLLEGE || data["college"]?.toString().equals(userCollege, ignoreCase = true) == true
            }

            val friendsFilter: (Map<String?, Any?>) -> Boolean = { data ->
                tab != LeaderboardTab.FRIENDS || friendIds.contains(data["userId"]) || data["userId"] == currentUserId
            }

            snapshot.documents
                .filter { doc ->
                    val data = doc.data
                    data != null && domainDsaFilter(data) && collegeFilter(data) && friendsFilter(data)
                }
                .take(50)
                .mapIndexed { index, doc ->
                    LeaderboardEntry(
                        userId = doc.id,
                        userName = doc.getString("name")
                            ?: doc.getString("displayName")
                            ?: "Anonymous",
                        score = doc.getLong("totalXP")?.toInt() ?: 0,
                        rank = index + 1,
                        isCurrentUser = doc.id == currentUserId,
                        college = doc.getString("college"),
                    )
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load leaderboard from Firestore")
            emptyList()
        }
    }

    override suspend fun getCurrentUserEntry(tab: LeaderboardTab): LeaderboardEntry? {
        val all = getLeaderboard(tab)
        return all.find { it.isCurrentUser }
    }

    override suspend fun togglePublicVisibility(isPublic: Boolean) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("isLeaderboardPublic", isPublic)
            .await()
    }
}

// Removed local LeaderboardTab enum as it exists in ILeaderboardRepository.kt

