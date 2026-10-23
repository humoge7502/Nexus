package com.nexus.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

private val Context.onboardingStore by preferencesDataStore(name = "onboarding")

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private companion object {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        const val DEST_AUTH = "auth"
        const val DEST_ONBOARDING = "onboarding"
        const val DEST_HOME = "main"
    }

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser

            if (currentUser == null) {
                _startDestination.value = DEST_AUTH
                return@launch
            }

            val localComplete = context.onboardingStore.data.first()[ONBOARDING_COMPLETE] ?: false

            if (localComplete) {
                _startDestination.value = DEST_HOME
                return@launch
            }

            try {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val isComplete = userDoc.getBoolean("isOnboardingComplete") ?: false

                if (isComplete) {
                    context.onboardingStore.edit { it[ONBOARDING_COMPLETE] = true }
                    _startDestination.value = DEST_HOME
                } else {
                    _startDestination.value = DEST_ONBOARDING
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to check onboarding state")
                _startDestination.value = DEST_ONBOARDING
            }
        }
    }
}
