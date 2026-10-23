package com.nexus.careermirror

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.NexusAnalytics
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.CareerMirrorRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.ScoreSnapshotRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.usecase.CareerMirrorGenerator
import com.nexus.domain.usecase.CareerMirrorGenerator.MirrorInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareerMirrorViewModel @Inject constructor(
  private val mirrorRepo: CareerMirrorRepository,
  private val scoreRepo: ScoreSnapshotRepository,
  private val dsaRepo: DSAProblemRepository,
  private val applicationRepo: ApplicationRepository,
  private val xpRepo: UserXpRepository,
  private val userPrefs: UserPreferences,
  private val analytics: NexusAnalytics,
  private val generator: CareerMirrorGenerator,
) : ViewModel() {

  private val _uiState = MutableStateFlow(CareerMirrorUiState())
  val uiState: StateFlow<CareerMirrorUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      val userId = userPrefs.userId.first()
      mirrorRepo.getByUserId(userId).collect { mirrors ->
        _uiState.update { it.copy(
          mirror = mirrors.firstOrNull(),
          previousMirror = mirrors.getOrNull(1),
          isLoading = false,
        ) }
      }
    }
  }

  fun generateMirror() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        val userId = userPrefs.userId.first()
        val (allDsa, allApps) = combine(
          dsaRepo.getAllProblems(),
          applicationRepo.getAllApplications(),
        ) { dsa, apps -> dsa to apps }.first()

        val dsaByDifficulty = allDsa.groupBy { it.difficulty.name }.mapValues { it.value.size }
        val dsaSolved = allDsa.count { it.status == DsaStatus.SOLVED }
        val interviews = allApps.count {
          it.status in listOf(ApplicationStatus.INTERVIEWING, ApplicationStatus.OFFERED)
        }
        val offers = allApps.count {
          it.status in listOf(ApplicationStatus.ACCEPTED, ApplicationStatus.OFFERED)
        }
        val previousSnapshots = scoreRepo.getRecentSnapshots(userId, limit = 10).first()
        val prevScore = previousSnapshots.getOrNull(previousSnapshots.size - 2)?.score
        val currentScore = previousSnapshots.lastOrNull()?.score ?: 500

        val input = MirrorInput(
          userId = userId, currentScore = currentScore,
          previousScore = prevScore, streakDays = 0,
          dsaSolvedThisPeriod = dsaSolved, dsaByDifficulty = dsaByDifficulty,
          applicationsThisPeriod = allApps.size, interviewsGot = interviews,
          offersReceived = offers, skillsGained = emptyList(),
          goalsCompleted = 0, milestonesCompleted = 0,
          certificationsCompleted = 0, aiMessagesSent = 0,
          topWeaknesses = emptyList(), missedOpportunityCount = 0,
          avgDailyPracticeMinutes = 45f,
        )

        val mirror = generator.generateMirror(input)
        mirrorRepo.insert(mirror)
        val scoreDelta = currentScore - (prevScore ?: currentScore)
        _uiState.update {
          it.copy(isLoading = false, mirror = mirror, currentScore = currentScore, scoreDelta = scoreDelta)
        }
        analytics.trackCareerMirrorGenerated(
          insightCount = mirror.keyInsights.size,
          actionItemCount = mirror.actionItems.size,
        )
        analytics.trackCareerMirrorViewed(mirror.periodLabel, scoreDelta)
        xpRepo.awardXP(25, "career_mirror_generated")
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown") }
      }
    }
  }
}

data class CareerMirrorUiState(
  val isLoading: Boolean = true,
  val mirror: com.nexus.domain.model.CareerMirror? = null,
  val previousMirror: com.nexus.domain.model.CareerMirror? = null,
  val currentScore: Int = 0,
  val scoreDelta: Int = 0,
  val error: String? = null,
)
