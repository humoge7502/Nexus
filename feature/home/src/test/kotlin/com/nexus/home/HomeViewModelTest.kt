package com.nexus.home

import com.nexus.common.ApiResult
import com.nexus.data.repository.CareerHitRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class HomeViewModelTest {

    @MockK
    private lateinit var internshipRepository: InternshipRepository

    @MockK
    private lateinit var careerHitRepository: CareerHitRepository

    @MockK
    private lateinit var userPreferences: UserPreferences

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { internshipRepository.getAllInternships() } returns flowOf(emptyList())
        coEvery { internshipRepository.refreshInternships() } returns ApiResult.Success(emptyList())
        every { careerHitRepository.getTodaysHit() } returns DailyCareerHit(
            id = "test_hit", title = "Test", tip = "Test tip",
            category = CareerHitCategory.CAREER_TIP, date = LocalDate.now(),
        )
        coEvery { careerHitRepository.isHitViewedToday() } returns false
        coEvery { careerHitRepository.hasXpBeenAwardedToday() } returns false
        coEvery { careerHitRepository.setTodaysHitId(any()) } returns Unit
        every { userPreferences.displayName } returns flowOf("Test User")
        viewModel = HomeViewModel(internshipRepository, careerHitRepository, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads user name from preferences`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assert(state.userName == "Test User")
    }

    @Test
    fun `loadSampleData updates opportunities from repository`() = runTest(testDispatcher) {
        val internships = listOf(
            Internship(
                id = "1", title = "Test", company = Company("TestCo"),
                location = Location("Remote"), stipend = null,
                duration = Duration(3), deadline = LocalDate.now(),
                applyUrl = "https://test.com", postedAt = Instant.now(),
                isRemote = true, isSaved = false,
            )
        )
        every { internshipRepository.getAllInternships() } returns flowOf(internships)

        viewModel = HomeViewModel(internshipRepository, careerHitRepository, userPreferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.todayOpportunities.size == 1)
        assert(state.todayOpportunities[0].title == "Test")
        assert(!state.isLoading)
    }

    @Test
    fun `career hit is loaded on init`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assert(state.careerHit != null)
        assert(state.careerHit?.id == "test_hit")
    }

    @Test
    fun `onCareerHitViewed marks as viewed and awards xp`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { careerHitRepository.markAsViewed() } returns Unit
        coEvery { careerHitRepository.markXpAwarded() } returns Unit

        viewModel.onCareerHitViewed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.careerHit?.isViewed == true)
        assert(state.careerHitXpAwarded)
        coVerify { careerHitRepository.markAsViewed() }
        coVerify { careerHitRepository.markXpAwarded() }
    }
}
