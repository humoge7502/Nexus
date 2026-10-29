package com.nexus.placementforecast

import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.InterviewReportRepository
import com.nexus.data.repository.PlacementForecastRepository
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.ScoreSnapshotRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.DSAProblem
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.PlacementForecast
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.model.StreakData
import com.nexus.domain.usecase.PlacementPredictionEngine
import com.nexus.domain.usecase.PlacementReadinessFeatures
import com.nexus.domain.usecase.PlacementReadinessModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PlacementForecastViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PlacementForecastRepository
    private lateinit var scoreRepository: ScoreSnapshotRepository
    private lateinit var predictionEngine: PlacementPredictionEngine
    private lateinit var dsaRepository: DSAProblemRepository
    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var certificationRepository: CertificationRepository
    private lateinit var portfolioRepository: PortfolioRepository
    private lateinit var interviewReportRepository: InterviewReportRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var userPrefs: UserPreferences

    private val generatedForecast = PlacementPredictionEngine.Forecast(
        tierLabel = "TIER 1 PRODUCT",
        companyTier = "TIER_1_PRODUCT",
        probabilityPct = 72,
        scoreGap = 30,
        gapItems = listOf("NEXUS Score is 30 points below tier threshold (750)"),
        velocityTrend = "rising",
        cohortBenchmark = "40/100 placed (40%)",
        coachMessage = "On track but needs focused push.",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        scoreRepository = mock()
        predictionEngine = mock()
        dsaRepository = mock()
        applicationRepository = mock()
        certificationRepository = mock()
        portfolioRepository = mock()
        interviewReportRepository = mock()
        streakRepository = mock()
        userPrefs = mock()

        whenever(repository.getForecasts(any())).thenReturn(flowOf(emptyList()))
        whenever(scoreRepository.getRecentSnapshots(any(), anyInt())).thenReturn(
            flowOf(
                listOf(
                    ScoreSnapshot(id = "s1", score = 600, recordedAt = 1_000L),
                    ScoreSnapshot(id = "s2", score = 720, recordedAt = 2_000L),
                )
            )
        )
        whenever(dsaRepository.getAllProblems()).thenReturn(flowOf(problems()))
        whenever(applicationRepository.getAllApplications()).thenReturn(flowOf(applications()))
        whenever(certificationRepository.getCompletedCertificationCount()).thenReturn(flowOf(2))
        whenever(portfolioRepository.getProjectCount()).thenReturn(flowOf(3))
        whenever(streakRepository.currentStreak).thenReturn(flowOf(StreakData(currentStreak = 9)))
        whenever(userPrefs.userId).thenReturn(flowOf("user-1"))
        whenever(userPrefs.userCollege).thenReturn(flowOf("IIT Delhi"))
        whenever(userPrefs.userYear).thenReturn(flowOf(3))
        whenever(userPrefs.userSkills).thenReturn(flowOf(setOf("Kotlin", "Python", "SQL", "C++", "React", "Android")))
        whenever(userPrefs.collegeTier).thenReturn(flowOf("TIER_2"))
        runBlocking {
            whenever(interviewReportRepository.getTotalCount()).thenReturn(4)
            whenever(
                predictionEngine.generateForecasts(any(), any(), any(), any(), any())
            ).thenReturn(listOf(generatedForecast))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PlacementForecastViewModel(
        repository = repository,
        scoreRepository = scoreRepository,
        predictionEngine = predictionEngine,
        dsaRepository = dsaRepository,
        applicationRepository = applicationRepository,
        certificationRepository = certificationRepository,
        portfolioRepository = portfolioRepository,
        interviewReportRepository = interviewReportRepository,
        streakRepository = streakRepository,
        userPrefs = userPrefs,
    )

    private fun problems() = listOf(
        DSAProblem(id = "p1", title = "Hard One", difficulty = DsaDifficulty.HARD, status = DsaStatus.SOLVED),
        DSAProblem(id = "p2", title = "Medium One", difficulty = DsaDifficulty.MEDIUM, status = DsaStatus.SOLVED),
        DSAProblem(id = "p3", title = "Medium Two", difficulty = DsaDifficulty.MEDIUM, status = DsaStatus.SOLVED),
        DSAProblem(id = "p4", title = "Attempted", difficulty = DsaDifficulty.EASY, status = DsaStatus.ATTEMPTED),
        DSAProblem(id = "p5", title = "Revisit", difficulty = DsaDifficulty.EASY, status = DsaStatus.REVISIT),
        DSAProblem(id = "p6", title = "Untouched", difficulty = DsaDifficulty.EASY, status = DsaStatus.NOT_ATTEMPTED),
    )

    private fun applications() = listOf(
        ApplicationStatus.NOT_APPLIED,
        ApplicationStatus.SAVED,
        ApplicationStatus.APPLIED,
        ApplicationStatus.INTERVIEW,
        ApplicationStatus.INTERVIEWING,
        ApplicationStatus.OFFERED,
        ApplicationStatus.ACCEPTED,
        ApplicationStatus.COMPLETED,
        ApplicationStatus.REJECTED,
    ).mapIndexed { index, status -> application("a$index", status) }

    private fun application(id: String, status: ApplicationStatus) = Application(
        id = id,
        opportunityId = "opp-$id",
        company = "Company",
        role = "SDE",
        opportunityType = "INTERNSHIP",
        status = status,
        matchScore = 0.8f,
        appliedDate = null,
        interviewDate = null,
        interviewRounds = 0,
        outcomeDate = null,
        outcomeNotes = null,
        salaryOffered = null,
        preparationNotes = null,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private fun expectedSignals(mockInterviews: Int = 4) = PlacementReadinessFeatures.RawSignals(
        nexusScore = 720,
        dsaSolved = 3,
        dsaHardSolved = 1,
        dsaMediumSolved = 2,
        dsaAttempted = 2,
        applications = 7,
        interviews = 2,
        offers = 3,
        certifications = 2,
        projects = 3,
        streakDays = 9,
        skillCount = 6,
        mockInterviews = mockInterviews,
        collegeTier = "TIER_2",
    )

    private fun expectedReadiness(mockInterviews: Int = 4): Int =
        PlacementReadinessModel.predictPercent(
            PlacementReadinessFeatures.fromRaw(expectedSignals(mockInterviews))
        )

    @Test
    fun `blank user surfaces a sign-in prompt`() = runTest(testDispatcher) {
        whenever(userPrefs.userId).thenReturn(flowOf(""))
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Sign in to generate a forecast.", state.error)
        assertNull(state.modelReadinessPct)
    }

    @Test
    fun `refresh maps real signals through the readiness model`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertEquals(720, state.currentScore)
        assertEquals(expectedReadiness(), state.modelReadinessPct)
        assertEquals(3, state.modelFactors.size)
        assertTrue(state.modelFactors.all { it.endsWith(" pts)") })
        assertEquals(0, state.whatIfMocks)
        assertNull(state.whatIfPct)
    }

    @Test
    fun `refresh persists one deterministic forecast per tier`() = runTest(testDispatcher) {
        viewModel()
        advanceUntilIdle()

        val captor = argumentCaptor<List<PlacementForecast>>()
        verify(repository).insertAll(captor.capture())
        val saved = captor.firstValue.single()
        assertEquals("user-1", saved.userId)
        assertTrue(saved.id.startsWith("forecast_user-1_TIER_1_PRODUCT_"))
        assertEquals(0.72f, saved.probabilityScore)
        assertTrue(saved.determiningFactors.contains("Score gap: 30 pts"))
        assertTrue(saved.determiningFactors.contains("30-day trend: rising"))
        assertTrue(saved.determiningFactors.contains("Cohort: 40/100 placed (40%)"))

        val extraCaptor = argumentCaptor<List<String>>()
        verify(predictionEngine).generateForecasts(
            eq("user-1"),
            eq(720),
            eq("IIT Delhi"),
            eq("YEAR_3"),
            extraCaptor.capture(),
        )
        assertEquals(
            "ML readiness: ${expectedReadiness()}% (model v${PlacementReadinessModel.VERSION})",
            extraCaptor.firstValue.first(),
        )
        assertEquals(4, extraCaptor.firstValue.size)

        verify(repository).deleteOlderThan(any(), anyLong())
    }

    @Test
    fun `stored forecasts are observed on the stream`() = runTest(testDispatcher) {
        whenever(repository.getForecasts(any())).thenReturn(
            flowOf(listOf(PlacementForecast(id = "f1", userId = "user-1", companyTier = "TIER_1_PRODUCT")))
        )
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.forecasts.size)
        assertEquals("f1", viewModel.uiState.value.forecasts.single().id)
    }

    @Test
    fun `what-if simulation recomputes readiness and clamps the range`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.updateWhatIfMocks(5)
        assertEquals(5, viewModel.uiState.value.whatIfMocks)
        assertEquals(updateReadinessFor(5), viewModel.uiState.value.whatIfPct)

        viewModel.updateWhatIfMocks(99)
        assertEquals(20, viewModel.uiState.value.whatIfMocks)
        assertEquals(updateReadinessFor(20), viewModel.uiState.value.whatIfPct)

        viewModel.updateWhatIfMocks(-3)
        assertEquals(0, viewModel.uiState.value.whatIfMocks)
        assertEquals(expectedReadiness(), viewModel.uiState.value.whatIfPct)
    }

    private fun updateReadinessFor(extraMocks: Int): Int = expectedReadiness(mockInterviews = 4 + extraMocks)

    @Test
    fun `what-if is a no-op before the first successful refresh`() = runTest(testDispatcher) {
        whenever(scoreRepository.getRecentSnapshots(any(), anyInt())).thenReturn(
            flow { throw IllegalStateException("score backend down") }
        )
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.updateWhatIfMocks(3)
        assertEquals(0, viewModel.uiState.value.whatIfMocks)
        assertNull(viewModel.uiState.value.whatIfPct)
    }

    @Test
    fun `refresh failure is caught and surfaced`() = runTest(testDispatcher) {
        whenever(scoreRepository.getRecentSnapshots(any(), anyInt())).thenReturn(
            flow { throw IllegalStateException("score backend down") }
        )
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("score backend down", state.error)
        assertEquals(0, state.currentScore)
    }
}
