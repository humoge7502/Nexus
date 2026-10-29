package com.nexus.settings.deletion

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountDeletionViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDeletionUiState())
    val uiState: StateFlow<AccountDeletionUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AccountDeletionEffect>()
    val effects: SharedFlow<AccountDeletionEffect> = _effects.asSharedFlow()

    fun confirmDeletion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("Not authenticated")

                deleteFirestoreData(userId)
                deleteStorageFiles(userId)

                firebaseAuth.currentUser?.delete()?.await()

                analyticsTracker.track("account_deleted")
                _effects.emit(AccountDeletionEffect.DeletionComplete)

            } catch (e: Exception) {
                Timber.e(e, "Account deletion failed")

                if (e is FirebaseAuthRecentLoginRequiredException) {
                    _effects.emit(AccountDeletionEffect.RequiresReAuthentication)
                } else {
                    _uiState.update { it.copy(
                        isDeleting = false,
                        error = "Deletion failed. Please try again or contact support@nexusapp.in"
                    )}
                }
            }
        }
    }

    private suspend fun deleteFirestoreData(userId: String) {
        val batch = firestore.batch()

        val subCollections = listOf(
            "goals", "achievements", "applications",
            "conversations", "projects", "notifications"
        )

        subCollections.forEach { collection ->
            val docs = firestore.collection("users")
                .document(userId)
                .collection(collection)
                .get()
                .await()
            docs.forEach { doc -> batch.delete(doc.reference) }
        }

        batch.delete(firestore.collection("users").document(userId))
        batch.delete(firestore.collection("publicProfiles").document(userId))

        batch.commit().await()
        Timber.d("Firestore data deleted for user: $userId")
    }

    private suspend fun deleteStorageFiles(userId: String) {
        try {
            firebaseStorage.reference
                .child("resumes/$userId")
                .listAll()
                .await()
                .items
                .forEach { it.delete().await() }

            firebaseStorage.reference
                .child("avatars/$userId")
                .listAll()
                .await()
                .items
                .forEach { it.delete().await() }
        } catch (e: Exception) {
            Timber.w(e, "Storage deletion partial failure — continuing")
        }
    }
}

@Immutable
data class AccountDeletionUiState(
    val isDeleting: Boolean = false,
    val error: String? = null,
)

sealed interface AccountDeletionEffect {
    data object DeletionComplete : AccountDeletionEffect
    data object RequiresReAuthentication : AccountDeletionEffect
}
