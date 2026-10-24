package com.nexus.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexus_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STUDY_STREAK = intPreferencesKey("study_streak")
        val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val GITHUB_USERNAME = stringPreferencesKey("github_username")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val USER_YEAR = intPreferencesKey("user_year")
        val USER_COLLEGE = stringPreferencesKey("user_college")
        val LAST_LOGIN_DATE = stringPreferencesKey("last_login_date")
        val COLLEGE_TIER = stringPreferencesKey("college_tier")
        val LEETCODE_USERNAME = stringPreferencesKey("leetcode_username")
        val LATEST_INSIGHT_TITLE = stringPreferencesKey("latest_insight_title")
        val LATEST_INSIGHT_BODY = stringPreferencesKey("latest_insight_body")
        val USER_TRIBE_ID = stringPreferencesKey("user_tribe_id")
    }

    val dataStore: DataStore<Preferences> get() = context.dataStore

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "SYSTEM"
    }

    val accentColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR] ?: "INDIGO"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    val studyStreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STUDY_STREAK] ?: 0
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun saveThemeMode(mode: String) = setThemeMode(mode)

    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCENT_COLOR] = color
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) = setNotificationsEnabled(enabled)

    suspend fun updateStreak(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[STUDY_STREAK] = days
        }
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AUTH_TOKEN]
    }

    val userId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_ID] ?: ""
    }

    val displayName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DISPLAY_NAME] ?: ""
    }

    val email: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EMAIL] ?: ""
    }

    val githubUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[GITHUB_USERNAME] ?: ""
    }

    val isPro: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PRO] ?: false
    }

    val userYear: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[USER_YEAR] ?: 1
    }

    val userCollege: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_COLLEGE] ?: ""
    }

    val lastLoginDate: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_LOGIN_DATE] ?: ""
    }

    val collegeTier: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[COLLEGE_TIER] ?: ""
    }

    val leetcodeUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LEETCODE_USERNAME] ?: ""
    }

    val latestInsightTitle: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LATEST_INSIGHT_TITLE] ?: ""
    }

    val userTribeId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_TRIBE_ID] ?: ""
    }

    val latestInsightBody: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LATEST_INSIGHT_BODY] ?: ""
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs -> prefs[AUTH_TOKEN] = token }
    }

    suspend fun saveGithubUsername(username: String) {
        context.dataStore.edit { prefs -> prefs[GITHUB_USERNAME] = username }
    }

    suspend fun saveIsPro(pro: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_PRO] = pro }
    }

    suspend fun saveUserYear(year: Int) {
        context.dataStore.edit { prefs -> prefs[USER_YEAR] = year }
    }

    suspend fun saveUserCollege(college: String) {
        context.dataStore.edit { prefs -> prefs[USER_COLLEGE] = college }
    }

    suspend fun setUserCollege(college: String) = saveUserCollege(college)

    suspend fun setUserTribeId(tribeId: String) {
        context.dataStore.edit { prefs -> prefs[USER_TRIBE_ID] = tribeId }
    }

    suspend fun saveLeetcodeUsername(username: String) {
        context.dataStore.edit { prefs -> prefs[LEETCODE_USERNAME] = username }
    }

    suspend fun saveLatestInsight(
        title: String,
        body: String,
        category: String,
        actionText: String? = null,
    ) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_INSIGHT_TITLE] = title
            prefs[LATEST_INSIGHT_BODY] = body
        }
    }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { prefs -> prefs[USER_ID] = id }
    }

    suspend fun saveDisplayName(name: String) {
        context.dataStore.edit { prefs -> prefs[DISPLAY_NAME] = name }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { prefs -> prefs[EMAIL] = email }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(AUTH_TOKEN)
            prefs.remove(USER_ID)
        }
    }
}
