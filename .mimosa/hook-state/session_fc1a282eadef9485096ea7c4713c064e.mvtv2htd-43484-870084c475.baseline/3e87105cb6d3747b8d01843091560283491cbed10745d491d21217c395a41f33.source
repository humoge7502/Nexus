package com.nexus.home

import com.nexus.common.ApiResult
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.usecase.PlacementReadinessCalculator
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.StreakData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: InternshipRepository
    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var certificationRepository: CertificationRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var readinessCalculator: PlacementReadinessCalculator
    private lateinit var userPreferences: UserPreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        applicationRepository = mock()
        certificationRepository = mock()
        streakRepository = mock()
        readinessCalculator = mock()
        userPreferences = mock()
        whenever(repository.getAllInternships()).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(repository.refreshInternships()).thenReturn(ApiResult.Success(emptyList()))
            whenever(streakRepository.checkAndUpdateStreaks()).thenReturn(Unit)
            whenever(readinessCalculator.calculate()).thenReturn(
                PlacementReadinessCalculator.PlacementReadiness(
                    score = 42,
                    level = "Building Up",
                    dsaScore = 0f,
                    skillScore = 0f,
                    consistencyScore = 2f,
                    applicationScore = 0f,
                    engagementScore = 0f,
                    actionItems = emptyList(),
                    estimatedPlacementMonth = "March 2027",
                )
            )
        }
        whenever(applicationRepository.getAllApplications()).thenReturn(flowOf(emptyList()))
        whenever(certificationRepository.getCompletedCertificationCount()).thenReturn(flowOf(0))
        whenever(streakRepository.currentStreak).thenReturn(flowOf(StreakData(currentStreak = 7)))
        whenever(userPreferences.displayName).thenReturn(flowOf("Test User"))
        whenever(userPreferences.tourCompleted).thenReturn(flowOf(true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(
        repository,
        applicationRepository,
        certificationRepository,
        streakRepository,
        readinessCalculator,
        userPreferences,
    )

    @Test
    fun `initial state exposes ui state immediately`() = runTest(testDispatcher) {
        assertNotNull(viewModel().uiState.value)
    }

    @Test
    fun `real display name replaces default`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals("Test User", viewModel.uiState.value.userName)
    }

    @Test
    fun `streak flows from the shared streak repository`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(7, viewModel.uiState.value.streakDays)
        assertEquals(7, viewModel.uiState.value.stats.currentStreak)
    }

    @Test
    fun `opening home records today's activity`() = runTest(testDispatcher) {
        viewModel()
        advanceUntilIdle()
        verify(streakRepository).checkAndUpdateStreaks()
    }

    @Test
    fun `completed certifications feed the home stats`() = runTest(testDispatcher) {
        whenever(certificationRepository.getCompletedCertificationCount()).thenReturn(flowOf(3))
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.stats.completedCerts)
    }

    @Test
    fun `career readiness comes from the readiness calculator`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(0.42f, viewModel.uiState.value.stats.careerReadinessScore, 0.001f)
        assertEquals("Building Up", viewModel.uiState.value.careerNextStep)
    }

    @Test
    fun `blank name falls back to aspirational default`() = runTest(testDispatcher) {
        whenever(userPreferences.displayName).thenReturn(flowOf(""))
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals("Future Engineer", viewModel.uiState.value.userName)
    }
}
