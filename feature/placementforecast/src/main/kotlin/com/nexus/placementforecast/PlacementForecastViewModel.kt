package com.nexus.placementforecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.InterviewReportRepository
import com.nexus.data.repository.PlacementForecastRepository
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.ScoreSnapshotRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.PlacementForecast
import com.nexus.domain.usecase.PlacementPredictionEngine
import com.nexus.domain.usecase.PlacementReadinessFeatures
import com.nexus.domain.usecase.PlacementReadinessModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForecastUiState(
  val isLoading: Boolean = true,
  val forecasts: List<PlacementForecast> = emptyList(),
  val currentScore: Int = 0,
  val scoreTrend: List<Float> = emptyList(),
  val modelReadinessPct: Int? = null,
  val modelFactors: List<String> = emptyList(),
  val modelVersion: String = PlacementReadinessModel.VERSION,
  val whatIfMocks: Int = 0,
  val whatIfPct: Int? = null,
  val error: String? = null,
)

@HiltViewModel
class PlacementForecastViewModel @Inject constructor(
  private val repository: PlacementForecastRepository,
  private val scoreRepository: ScoreSnapshotRepository,
  private val predictionEngine: PlacementPredictionEngine,
  private val dsaRepository: DSAProblemRepository,
  private val applicationRepository: ApplicationRepository,
  private val certificationRepository: CertificationRepository,
  private val portfolioRepository: PortfolioRepository,
  private val interviewReportRepository: InterviewReportRepository,
  private val streakRepository: StreakRepository,
  private val userPrefs: UserPreferences,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ForecastUiState())
  val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

  private var lastSignals: PlacementReadinessFeatures.RawSignals? = null
  private var lastModelPct: Int? = null

  init {
    observeForecasts()
    refresh()
  }

  private fun observeForecasts() {
    viewModelScope.launch {
      val userId = userPrefs.userId.first()
      repository.getForecasts(userId).collect { list ->
        _uiState.update { it.copy(forecasts = list, isLoading = false) }
      }
    }
  }

  fun refresh() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        val userId = userPrefs.userId.first()
        if (userId.isBlank()) {
          _uiState.update { it.copy(isLoading = false, error = "Sign in to generate a forecast.") }
          return@launch
        }

        val snapshots = scoreRepository.getRecentSnapshots(userId, limit = 60).first()
        val currentScore = snapshots.maxByOrNull { it.recordedAt }?.score ?: DEFAULT_SCORE
        val scoreTrend = snapshots.sortedBy { it.recordedAt }.map { it.score.toFloat() }
        val collegeId = userPrefs.userCollege.first().ifBlank { "unknown" }
        val year = userPrefs.userYear.first()

        val signals = coroutineScope {
          val dsaDeferred = async { dsaRepository.getAllProblems().first() }
          val appsDeferred = async { applicationRepository.getAllApplications().first() }
          val certsDeferred = async { certificationRepository.getCompletedCertificationCount().first() }
          val projectsDeferred = async { portfolioRepository.getProjectCount().first() }
          val mocksDeferred = async { interviewReportRepository.getTotalCount() }

          val dsa = dsaDeferred.await()
          val apps = appsDeferred.await()
          val submitted = apps.count {
            it.status != ApplicationStatus.NOT_APPLIED && it.status != ApplicationStatus.SAVED
          }
          PlacementReadinessFeatures.RawSignals(
            nexusScore = currentScore,
            dsaSolved = dsa.count { it.status == DsaStatus.SOLVED },
            dsaHardSolved = dsa.count {
              it.status == DsaStatus.SOLVED && it.difficulty == DsaDifficulty.HARD
            },
            dsaMediumSolved = dsa.count {
              it.status == DsaStatus.SOLVED && it.difficulty == DsaDifficulty.MEDIUM
            },
            dsaAttempted = dsa.count {
              it.status == DsaStatus.ATTEMPTED || it.status == DsaStatus.REVISIT
            },
            applications = submitted,
            interviews = apps.count {
              it.status == ApplicationStatus.INTERVIEW ||
                it.status == ApplicationStatus.INTERVIEWING
            },
            offers = apps.count {
              it.status == ApplicationStatus.OFFERED ||
                it.status == ApplicationStatus.ACCEPTED ||
                it.status == ApplicationStatus.COMPLETED
            },
            certifications = certsDeferred.await(),
            projects = projectsDeferred.await(),
            streakDays = streakRepository.currentStreak.first().currentStreak,
            skillCount = userPrefs.userSkills.first().size,
            mockInterviews = mocksDeferred.await(),
            collegeTier = userPrefs.collegeTier.first(),
          )
        }

        val features = PlacementReadinessFeatures.fromRaw(signals)
        val modelPct = PlacementReadinessModel.predictPercent(features)
        lastSignals = signals
        lastModelPct = modelPct
        val factors = PlacementReadinessModel.topFactors(features, limit = 3).map { factor ->
          val sign = if (factor.deltaPoints >= 0) "+" else ""
          "${factor.label} ($sign${factor.deltaPoints} pts)"
        }
        val extraFactors = buildList {
          add("ML readiness: $modelPct% (model v${PlacementReadinessModel.VERSION})")
          addAll(factors)
        }

        val generated = predictionEngine.generateForecasts(
          userId = userId,
          currentScore = currentScore,
          collegeId = collegeId,
          semester = "YEAR_$year",
          extraFactors = extraFactors,
        )

        val now = System.currentTimeMillis()
        val dayBucket = now / DAY_MS
        val forecasts = generated.map { forecast ->
          PlacementForecast(
            // Deterministic per tier/day so refreshing replaces instead of
            // appending a duplicate row for every visit.
            id = "forecast_${userId}_${forecast.companyTier}_$dayBucket",
            userId = userId,
            companyTier = forecast.companyTier,
            probabilityScore = forecast.probabilityPct / 100f,
            determiningFactors = buildList {
              add("Score gap: ${forecast.scoreGap} pts")
              addAll(forecast.gapItems)
              add("30-day trend: ${forecast.velocityTrend}")
              add("Cohort: ${forecast.cohortBenchmark}")
              add(forecast.coachMessage)
            }.distinct(),
            calculatedAt = now,
          )
        }
        repository.insertAll(forecasts)
        // Keep only today's snapshot so the list never grows without bound.
        repository.deleteOlderThan(userId, dayBucket * DAY_MS)
        _uiState.update {
          it.copy(
            isLoading = false,
            currentScore = currentScore,
            scoreTrend = scoreTrend,
            modelReadinessPct = modelPct,
            modelFactors = factors,
            whatIfMocks = 0,
            whatIfPct = null,
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, error = e.message ?: "Unable to generate a forecast.")
        }
      }
    }
  }

  /** Counterfactual: readiness if the student added N more mock interviews. */
  fun updateWhatIfMocks(count: Int) {
    val signals = lastSignals ?: return
    val extra = count.coerceIn(0, MAX_WHAT_IF_MOCKS)
    val simulated = signals.copy(mockInterviews = signals.mockInterviews + extra)
    val pct = PlacementReadinessModel.predictPercent(
      PlacementReadinessFeatures.fromRaw(simulated)
    )
    _uiState.update { it.copy(whatIfMocks = extra, whatIfPct = pct) }
  }

  private companion object {
    const val DEFAULT_SCORE = 500
    const val DAY_MS = 86_400_000L
    const val MAX_WHAT_IF_MOCKS = 20
  }
}
