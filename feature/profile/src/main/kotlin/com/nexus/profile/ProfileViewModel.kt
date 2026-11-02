package com.nexus.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.common.Constants
import com.nexus.data.repository.AchievementRepository
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.GoalRepository
import com.nexus.data.repository.GitHubRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Achievement
import com.nexus.domain.model.GoalCategory
import com.nexus.domain.model.Skill
import com.nexus.domain.repository.ISkillGraphRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val university: String = "",
    val year: Int = 1,
    val bio: String = "",
    val nexusScore: Int = 0,
    val level: Int = 1,
    val totalXP: Int = 0,
    val streakDays: Int = 0,
    val applicationsCount: Int = 0,
    val interviewingCount: Int = 0,
    val offersCount: Int = 0,
    val certificationsCount: Int = 0,
    val skills: List<Skill> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val githubUsername: String? = null,
    val linkedinUrl: String? = null,
    val referralCode: String = "",
    val referralLink: String = "",
    val isPro: Boolean = false,
    val error: String? = null,
    val xpToNextLevel: Int = 100,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val xpRepository: UserXpRepository,
    private val gitHubRepository: GitHubRepository,
    private val applicationRepository: ApplicationRepository,
    private val goalRepository: GoalRepository,
    private val skillGraphRepository: ISkillGraphRepository,
    private val streakRepository: StreakRepository,
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var hasAwardedProfileXP = false

    /** Catalog skills the student has not claimed yet, offered as chips in the editor. */
    private val _skillSuggestions = MutableStateFlow<List<String>>(emptyList())
    val skillSuggestions: StateFlow<List<String>> = _skillSuggestions.asStateFlow()

    /** Names the student declared in onboarding — the set the matcher actually scores. */
    private val _declaredSkills = MutableStateFlow<List<String>>(emptyList())
    val declaredSkills: StateFlow<List<String>> = _declaredSkills.asStateFlow()

    init {
        loadProfile()
        loadSkillSuggestions()
        awardProfileCompletionOnce()
        viewModelScope.launch {
            userPreferences.userSkills.collect { _declaredSkills.value = it.toList().sorted() }
        }
    }

    private fun loadSkillSuggestions() {
        viewModelScope.launch {
            _skillSuggestions.value = runCatching {
                skillGraphRepository.getRecommendedSkills().map { it.name }
            }.getOrDefault(_skillSuggestions.value)
        }
    }

    /**
     * Writes the declared set to both stores: DataStore drives opportunity
     * matching, the graph drives the radar. Leaving one behind is how a profile
     * starts disagreeing with itself.
     */
    fun saveSkills(names: List<String>) {
        val cleaned = names.map { it.trim() }.filter { it.isNotEmpty() }
        viewModelScope.launch {
            userPreferences.setUserSkills(cleaned)
            skillGraphRepository.setDeclaredSkills(cleaned)
            _declaredSkills.value = cleaned
            loadSkillSuggestions()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val identity = combine(
                userPreferences.displayName,
                userPreferences.userId,
                userPreferences.githubUsername,
                userPreferences.userCollege,
                userPreferences.userYear,
            ) { displayName, userId, githubUsername, college, year ->
                Identity(displayName, userId, githubUsername, college, year)
            }

            val progression = combine(
                xpRepository.totalXP,
                xpRepository.currentLevel,
                xpRepository.xpToNextLevel,
                streakRepository.currentStreak.map { it.currentStreak },
                userPreferences.isPro,
            ) { totalXP, level, xpToNext, streak, isPro ->
                Progression(totalXP, level, xpToNext, streak, isPro)
            }

            val activity = combine(
                applicationRepository.getApplicationCounts(),
                // Completed certification goals are the user's own progress;
                // catalog size would show 57 "certs" for a brand-new account.
                goalRepository.getAllGoals().map { goals ->
                    goals.count { it.category == GoalCategory.CERTIFICATION && it.isCompleted }
                },
                userSkillsFlow(),
            ) { counts, certifications, skills ->
                Activity(counts.total, counts.interviewing, counts.offered, certifications, skills)
            }

            val badges = achievementRepository.getAll()
                .map { achievements ->
                    achievements.sortedWith(
                        compareByDescending<Achievement> { it.isUnlocked }
                            .thenByDescending {
                                if (it.maxProgress > 0f) it.progress / it.maxProgress else 0f
                            }
                    ).take(3)
                }
                .catch { emit(emptyList()) }

            combine(identity, progression, activity, badges) { id, prog, act, badges ->
                val referralCode = id.userId.takeLast(8).uppercase()
                ProfileUiState(
                    isLoading = false,
                    name = id.displayName.ifBlank { "User" },
                    university = id.college,
                    year = id.year,
                    githubUsername = id.githubUsername.ifBlank { null },
                    level = prog.level,
                    totalXP = prog.totalXP,
                    streakDays = prog.streak,
                    xpToNextLevel = prog.xpToNext,
                    applicationsCount = act.applications,
                    interviewingCount = act.interviewing,
                    offersCount = act.offers,
                    certificationsCount = act.certifications,
                    skills = act.skills,
                    achievements = badges,
                    isPro = prog.isPro,
                    referralCode = referralCode,
                    referralLink = "https://nexusapp.in/referral/$referralCode",
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun userSkillsFlow(): Flow<List<Skill>> =
        skillGraphRepository.observeUserSkills().catch { emit(emptyList()) }

    private fun awardProfileCompletionOnce() {
        viewModelScope.launch {
            val name = userPreferences.displayName.first()
            val github = userPreferences.githubUsername.first()
            if (!hasAwardedProfileXP && name.isNotBlank() && github.isNotBlank()) {
                hasAwardedProfileXP = true
                xpRepository.awardXP(Constants.XP_MAX_PROFILE, "profile_completed")
            }
        }
    }

    fun onReferralShared() {
        viewModelScope.launch {
            xpRepository.awardXP(Constants.XP_PER_REFERRAL, "referral_shared")
        }
    }

    private data class Identity(
        val displayName: String,
        val userId: String,
        val githubUsername: String,
        val college: String,
        val year: Int,
    )

    private data class Progression(
        val totalXP: Int,
        val level: Int,
        val xpToNext: Int,
        val streak: Int,
        val isPro: Boolean,
    )

    private data class Activity(
        val applications: Int,
        val interviewing: Int,
        val offers: Int,
        val certifications: Int,
        val skills: List<Skill>,
    )
}
