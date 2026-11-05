package com.nexus.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.common.ApiResult
import com.nexus.common.Constants
import com.nexus.data.repository.AchievementRepository
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.GoalRepository
import com.nexus.data.repository.PlatformRepository
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
    private val platformRepository: PlatformRepository,
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

    /** Live numbers pulled from the handles connected in Settings. */
    private val _platformStats = MutableStateFlow(PlatformStatsUiState())
    val platformStats: StateFlow<PlatformStatsUiState> = _platformStats.asStateFlow()

    init {
        loadProfile()
        loadSkillSuggestions()
        loadPlatformStats()
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
                combine(
                    userPreferences.displayName,
                    userPreferences.userId,
                    userPreferences.githubUsername,
                    userPreferences.linkedinUrl,
                ) { displayName, userId, githubUsername, linkedinUrl ->
                    IdentityCore(displayName, userId, githubUsername, linkedinUrl)
                },
                userPreferences.userCollege,
                userPreferences.userYear,
            ) { core, college, year ->
                Identity(core.displayName, core.userId, core.githubUsername, college, year, core.linkedinUrl)
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
                    linkedinUrl = id.linkedinUrl.ifBlank { null },
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
            }.catch { error ->
                // Without this, a failed read leaves the skeleton spinning
                // forever and the coroutine dies silently in the scope.
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Profile could not be loaded",
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadProfile()
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
        val linkedinUrl: String,
    )

    private data class IdentityCore(
        val displayName: String,
        val userId: String,
        val githubUsername: String,
        val linkedinUrl: String,
    )

    /**
     * Fetches each connected platform once per screen entry through the
     * platform façade. Repositories carry a 10-minute TTL cache and
     * PlatformSyncWorker keeps it warm in the background, so re-entries are
     * cheap. A per-platform failure leaves that card off quietly — never a
     * fake zero.
     */
    private fun loadPlatformStats() {
        viewModelScope.launch {
            val github = userPreferences.githubUsername.first().trim().trimStart('@')
            val leetcode = userPreferences.leetcodeUsername.first().trim()
            val codeforces = userPreferences.codeforcesUsername.first().trim()
            if (github.isBlank() && leetcode.isBlank() && codeforces.isBlank()) return@launch

            val snap = platformRepository.snapshot(
                githubHandle = github,
                leetcodeHandle = leetcode,
                codeforcesHandle = codeforces,
            )

            snap.github?.let { g ->
                val profile = g.profile as? ApiResult.Success ?: return@let
                val repos = g.repos as? ApiResult.Success ?: return@let
                _platformStats.value = _platformStats.value.copy(
                    github = GitHubStats(
                        handle = profile.data.login,
                        publicRepos = profile.data.publicRepos,
                        followers = profile.data.followers,
                        totalStars = repos.data.sumOf { it.stars },
                        recentCommits = g.activity.dataOrNull?.totalCommits ?: 0,
                        streakDays = g.activity.dataOrNull?.currentStreakDays ?: 0,
                    ),
                )
            }

            (snap.leetcode as? ApiResult.Success)?.let { stats ->
                _platformStats.value = _platformStats.value.copy(
                    leetcode = LeetCodeCard(
                        handle = leetcode,
                        totalSolved = stats.data.totalSolved,
                        easySolved = stats.data.easySolved,
                        mediumSolved = stats.data.mediumSolved,
                        hardSolved = stats.data.hardSolved,
                        ranking = stats.data.ranking,
                    ),
                )
            }

            snap.codeforces?.let { c ->
                val profile = c.profile as? ApiResult.Success ?: return@let
                val points = (c.ratingHistory as? ApiResult.Success)?.data.orEmpty()
                _platformStats.value = _platformStats.value.copy(
                    codeforces = CodeforcesCard(
                        handle = profile.data.handle,
                        rating = profile.data.rating,
                        maxRating = profile.data.maxRating,
                        rank = profile.data.rank,
                        lastDelta = points.lastOrNull()?.delta,
                        trend = points.map { it.newRating.toFloat() },
                        contestsCounted = points.size,
                    ),
                )
            }
        }
    }

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

@Immutable
data class PlatformStatsUiState(
    val github: GitHubStats? = null,
    val leetcode: LeetCodeCard? = null,
    val codeforces: CodeforcesCard? = null,
) {
    val hasAny: Boolean get() = github != null || leetcode != null || codeforces != null
}

@Immutable
data class GitHubStats(
    val handle: String,
    val publicRepos: Int,
    val followers: Int,
    val totalStars: Int,
    /** Commits observed in the public events feed (tokenless GitHub has no calendar). */
    val recentCommits: Int,
    val streakDays: Int,
)

@Immutable
data class LeetCodeCard(
    val handle: String,
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
    val ranking: Int,
)

@Immutable
data class CodeforcesCard(
    val handle: String,
    val rating: Int?,
    val maxRating: Int?,
    val rank: String?,
    val lastDelta: Int?,
    /** Contest rating series, oldest first — rendered as the trend line. */
    val trend: List<Float> = emptyList(),
    val contestsCounted: Int = 0,
)
