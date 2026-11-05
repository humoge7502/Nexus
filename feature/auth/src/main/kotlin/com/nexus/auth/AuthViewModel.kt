package com.nexus.auth

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nexus.common.ApiResult
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

/**
 * Why a social sign-in ended without a session.
 *
 * The screen needs the reason, not the status: an interrupted Google prompt
 * surfaces as `ApiException(statusCode=12501)` whose message is the literal
 * string "Task:canceled", and repeating that to a student is not feedback.
 */
enum class AuthFailure { Cancelled, Unavailable }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    @Named("googleWebClientId") private val googleWebClientId: String,
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null

    private val _authState = MutableStateFlow<ApiResult<Boolean>>(ApiResult.Loading)
    val authState: StateFlow<ApiResult<Boolean>> = _authState.asStateFlow()

    private val _failure = MutableStateFlow<AuthFailure?>(null)
    val failure: StateFlow<AuthFailure?> = _failure.asStateFlow()

    init {
        checkExistingAuth()
    }

    private fun checkExistingAuth() {
        if (auth.currentUser != null) {
            _authState.value = ApiResult.Success(true)
        } else {
            _authState.value = ApiResult.Success(false)
        }
    }

    fun initGoogleSignIn(activity: Activity) {
        _failure.value = null
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleWebClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.user?.let { saveUserData(it.uid, it.displayName ?: "", it.email ?: "") }
            _failure.value = null
            if (authResult.user != null) {
                _authState.value = ApiResult.Success(true)
                true
            } else {
                _authState.value = ApiResult.Error("Google sign-in returned no user")
                _failure.value = AuthFailure.Unavailable
                false
            }
        } catch (e: ApiException) {
            // Closing the account picker is a choice, not a fault worth an error
            // tone; the prompt simply having nothing to return is a different one.
            val cancelled = e.statusCode == CommonStatusCodes.CANCELED ||
                e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED
            _failure.value = if (cancelled) AuthFailure.Cancelled else AuthFailure.Unavailable
            _authState.value = ApiResult.Error(e.message ?: "Google sign-in failed")
            false
        } catch (e: Exception) {
            _failure.value = AuthFailure.Unavailable
            _authState.value = ApiResult.Error(e.message ?: "Google sign-in failed")
            false
        }
    }

    /** The band is about one attempt; a new one, or moving on, retires it. */
    fun dismissFailure() {
        _failure.value = null
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _failure.value = null
            _authState.value = ApiResult.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { saveUserData(it.uid, it.displayName ?: email, email) }
                _authState.value = ApiResult.Success(true)
            } catch (e: Exception) {
                _authState.value = ApiResult.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _failure.value = null
            _authState.value = ApiResult.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { saveUserData(it.uid, it.displayName ?: email, email) }
                _authState.value = ApiResult.Success(true)
            } catch (e: Exception) {
                _authState.value = ApiResult.Error(e.message ?: "Sign-up failed")
            }
        }
    }

    private suspend fun saveUserData(uid: String, name: String, email: String) {
        userPreferences.saveUserId(uid)
        userPreferences.saveDisplayName(name)
        userPreferences.saveEmail(email)
        auth.currentUser?.getIdToken(false)?.await()?.token?.let {
            userPreferences.saveAuthToken(it)
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient?.signOut()
        viewModelScope.launch {
            userPreferences.clearAuth()
        }
        _failure.value = null
        _authState.value = ApiResult.Success(false)
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
    fun isLoggedIn(): Boolean = auth.currentUser != null
}
