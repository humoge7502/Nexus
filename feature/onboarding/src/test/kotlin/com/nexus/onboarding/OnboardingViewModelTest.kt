package com.nexus.onboarding

import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.repository.XPAwardResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferences: UserPreferences
    private lateinit var xpRepository: UserXpRepository
    private lateinit var analyticsTracker: AnalyticsTracker

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = mock()
        xpRepository = mock()
        analyticsTracker = mock()
        whenever(userPreferences.onboardingCompleted).thenReturn(flowOf(false))
        runBlocking {
            whenever(xpRepository.awardXP(any(), any())).thenReturn(
                XPAwardResult(
                    xpAwarded = Constants.XP_ONBOARDING_COMPLETE,
                    totalXP = Constants.XP_ONBOARDING_COMPLETE,
                    leveledUp = false,
                    newLevel = 1,
                    source = "onboarding",
                )
            )
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnboardingViewModel(userPreferences, xpRepository, analyticsTracker)

    @Test
    fun `initial state is empty`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        assertEquals(OnboardingUiState(), viewModel.uiState.value)
    }

    @Test
    fun `profile fields update independently`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.updateName("Aarav")
        viewModel.updateCollege("IIT Delhi")
        viewModel.updateYear("3rd Year")
        viewModel.updateCareerGoal("SDE")

        val state = viewModel.uiState.value
        assertEquals("Aarav", state.userName)
        assertEquals("IIT Delhi", state.college)
        assertEquals("3rd Year", state.academicYear)
        assertEquals("SDE", state.careerGoal)
    }

    @Test
    fun `skills stop at five selections and can be removed`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        skillOptions.take(6).forEach(viewModel::toggleSkill)
        assertEquals(5, viewModel.uiState.value.skills.size)

        viewModel.toggleSkill(skillOptions.first())
        assertEquals(4, viewModel.uiState.value.skills.size)
        assertTrue(skillOptions[1] in viewModel.uiState.value.skills)
    }

    @Test
    fun `interests toggle on and off`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.toggleInterest("Hackathons")
        viewModel.toggleInterest("Research")
        assertEquals(listOf("Hackathons", "Research"), viewModel.uiState.value.interests)

        viewModel.toggleInterest("Hackathons")
        assertEquals(listOf("Research"), viewModel.uiState.value.interests)
    }

    @Test
    fun `save persists trimmed profile with year index and awards xp`() = runTest(testDispatcher) {
        var completed = false
        val viewModel = viewModel()
        viewModel.updateName("Aarav")
        viewModel.updateCollege("  IIT Delhi  ")
        viewModel.updateYear("3rd Year")
        viewModel.toggleSkill("Kotlin")
        viewModel.toggleSkill("Python")
        viewModel.toggleInterest("Internships")
        viewModel.updateCareerGoal("SDE")

        viewModel.saveAndComplete { completed = true }
        advanceUntilIdle()

        verify(userPreferences).saveOnboardingProfile(
            name = "Aarav",
            college = "IIT Delhi",
            year = 3,
            skills = listOf("Kotlin", "Python"),
            interests = listOf("Internships"),
            careerGoal = "SDE",
        )
        verify(xpRepository).awardXP(Constants.XP_ONBOARDING_COMPLETE, "onboarding")
        verify(analyticsTracker).track(
            "onboarding_complete",
            mapOf("role" to "SDE", "skills" to "2", "interests" to "1"),
        )
        assertTrue(completed)
    }

    @Test
    fun `blank name and unknown year fall back to defaults`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.saveAndComplete { }
        advanceUntilIdle()

        verify(userPreferences).saveOnboardingProfile(
            name = "Student",
            college = "",
            year = 1,
            skills = emptyList(),
            interests = emptyList(),
            careerGoal = "",
        )
    }

    @Test
    fun `skip saves neutral profile without xp`() = runTest(testDispatcher) {
        var completed = false
        val viewModel = viewModel()
        viewModel.updateName("Ignored")

        viewModel.skipAndComplete { completed = true }
        advanceUntilIdle()

        verify(userPreferences).saveOnboardingProfile(
            name = "Student",
            college = "",
            year = 1,
            skills = emptyList(),
            interests = emptyList(),
            careerGoal = "",
        )
        verify(analyticsTracker).track("onboarding_skipped")
        verify(xpRepository, never()).awardXP(any(), any())
        assertTrue(completed)
    }

    @Test
    fun `onboarding completed stays null until datastore emits`() = runTest(testDispatcher) {
        whenever(userPreferences.onboardingCompleted).thenReturn(flowOf(true))
        val viewModel = viewModel()
        assertNull(viewModel.onboardingCompleted.value)

        val collector = launch { viewModel.onboardingCompleted.collect { } }
        advanceUntilIdle()
        assertEquals(true, viewModel.onboardingCompleted.value)
        collector.cancel()
    }
}
