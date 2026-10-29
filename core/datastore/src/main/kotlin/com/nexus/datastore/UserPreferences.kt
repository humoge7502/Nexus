package com.nexus.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexus.datastore.security.TokenCipher
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
        val AMOLED_ENABLED = booleanPreferencesKey("amoled_enabled")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val GITHUB_USERNAME = stringPreferencesKey("github_username")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val USER_YEAR = intPreferencesKey("user_year")
        val USER_COLLEGE = stringPreferencesKey("user_college")
        val USER_SKILLS = stringSetPreferencesKey("user_skills")
        val USER_INTERESTS = stringSetPreferencesKey("user_interests")
        val CAREER_GOAL = stringPreferencesKey("career_goal")
        val LAST_LOGIN_DATE = stringPreferencesKey("last_login_date")
        val COLLEGE_TIER = stringPreferencesKey("college_tier")
        val TOUR_COMPLETED = booleanPreferencesKey("tour_completed")
        val LEETCODE_USERNAME = stringPreferencesKey("leetcode_username")
        val LATEST_INSIGHT_TITLE = stringPreferencesKey("latest_insight_title")
        val LATEST_INSIGHT_BODY = stringPreferencesKey("latest_insight_body")
        val USER_TRIBE_ID = stringPreferencesKey("user_tribe_id")
        val TRIBE_GOAL = stringPreferencesKey("tribe_goal")
        val TRIBE_POD_SIZE = intPreferencesKey("tribe_pod_size")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    val dataStore: DataStore<Preferences> get() = context.dataStore

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        // Legacy builds stored AMOLED as a theme mode; it is now an overlay
        // on dark mode so switching dark mode off never leaves AMOLED active.
        if (prefs[THEME_MODE] == "AMOLED") "DARK" else prefs[THEME_MODE] ?: "SYSTEM"
    }

    val amoledEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AMOLED_ENABLED] ?: (prefs[THEME_MODE] == "AMOLED")
    }

    val accentColor: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR] ?: "DEFAULT"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
            if (mode != "DARK") prefs[AMOLED_ENABLED] = false
        }
    }

    suspend fun saveThemeMode(mode: String) = setThemeMode(mode)

    suspend fun setAmoledEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AMOLED_ENABLED] = enabled
            if (enabled) prefs[THEME_MODE] = "DARK"
        }
    }

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

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AUTH_TOKEN]?.let { TokenCipher.decrypt(it) }
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

    val userSkills: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[USER_SKILLS] ?: emptySet()
    }

    val userInterests: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[USER_INTERESTS] ?: emptySet()
    }

    val careerGoal: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CAREER_GOAL] ?: ""
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

    val tourCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TOUR_COMPLETED] ?: false
    }

    suspend fun setTourCompleted() {
        context.dataStore.edit { prefs -> prefs[TOUR_COMPLETED] = true }
    }

    val tribeGoal: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[TRIBE_GOAL] ?: ""
    }

    val tribePodSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TRIBE_POD_SIZE] ?: 5
    }

    val latestInsightBody: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LATEST_INSIGHT_BODY] ?: ""
    }

    suspend fun saveAuthToken(token: String) {
        val encrypted = runCatching { TokenCipher.encrypt(token) }.getOrNull()
            ?: throw IllegalStateException("Unable to encrypt auth token")
        context.dataStore.edit { prefs -> prefs[AUTH_TOKEN] = encrypted }
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

    suspend fun saveOnboardingProfile(
        name: String,
        college: String,
        year: Int,
        skills: Collection<String>,
        interests: Collection<String>,
        careerGoal: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[DISPLAY_NAME] = name
            prefs[USER_COLLEGE] = college
            prefs[USER_YEAR] = year
            prefs[USER_SKILLS] = skills.toSet()
            prefs[USER_INTERESTS] = interests.toSet()
            prefs[CAREER_GOAL] = careerGoal
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setUserCollege(college: String) = saveUserCollege(college)

    suspend fun setUserTribeId(tribeId: String) {
        context.dataStore.edit { prefs -> prefs[USER_TRIBE_ID] = tribeId }
    }

    suspend fun saveTribeSetup(goal: String, podSize: Int) {
        context.dataStore.edit { prefs ->
            prefs[TRIBE_GOAL] = goal.trim()
            prefs[TRIBE_POD_SIZE] = podSize.coerceIn(3, 7)
        }
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

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { prefs -> prefs[FCM_TOKEN] = token }
    }

    val fcmToken: Flow<String?> = context.dataStore.data.map { prefs -> prefs[FCM_TOKEN] }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(AUTH_TOKEN)
            prefs.remove(USER_ID)
        }
    }
}
