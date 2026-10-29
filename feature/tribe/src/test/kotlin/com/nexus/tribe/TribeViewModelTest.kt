package com.nexus.tribe

import com.nexus.analytics.NexusAnalytics
import com.nexus.data.repository.CohortIntelligenceRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.CohortEntry
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
import org.junit.Assert.assertFalse
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
class TribeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPrefs: UserPreferences
    private lateinit var analytics: NexusAnalytics
    private lateinit var cohortRepository: CohortIntelligenceRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userPrefs = mock()
        analytics = mock()
        cohortRepository = mock()
        whenever(userPrefs.userTribeId).thenReturn(flowOf(""))
        whenever(userPrefs.userCollege).thenReturn(flowOf(""))
        whenever(userPrefs.tribeGoal).thenReturn(flowOf(""))
        whenever(userPrefs.tribePodSize).thenReturn(flowOf(5))
        whenever(cohortRepository.getByCollege(any())).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(userPrefs.setUserTribeId(any())).thenReturn(Unit)
            whenever(userPrefs.setUserCollege(any())).thenReturn(Unit)
            whenever(userPrefs.saveTribeSetup(any(), any())).thenReturn(Unit)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = TribeViewModel(userPrefs, analytics, cohortRepository)

    @Test
    fun `loadTribe surfaces saved setup and newest cohort`() = runTest(testDispatcher) {
        whenever(userPrefs.userTribeId).thenReturn(flowOf("tribe_iitb"))
        whenever(userPrefs.userCollege).thenReturn(flowOf("IIT Bombay"))
        whenever(userPrefs.tribeGoal).thenReturn(flowOf("SDE"))
        whenever(userPrefs.tribePodSize).thenReturn(flowOf(6))
        whenever(cohortRepository.getByCollege("IIT Bombay")).thenReturn(
            flowOf(
                listOf(
                    CohortEntry(id = "older", collegeId = "IIT Bombay", updatedAt = 10L),
                    CohortEntry(id = "newest", collegeId = "IIT Bombay", updatedAt = 20L),
                )
            )
        )

        val viewModel = viewModel()
        viewModel.loadTribe()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isInTribe)
        assertEquals("tribe_iitb", state.tribeId)
        assertEquals("IIT Bombay", state.college)
        assertEquals("SDE", state.goal)
        assertEquals(6, state.podSize)
        assertEquals("newest", state.cohort?.id)
    }

    @Test
    fun `blank college is rejected without touching preferences`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.createOrJoinTribe("   ", "SDE", 5)
        advanceUntilIdle()

        assertEquals("Enter your college to find your tribe.", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isInTribe)
        verify(analytics, never()).trackTribeJoined(any(), any())
    }

    @Test
    fun `join slugifies college, persists setup and tracks analytics`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.createOrJoinTribe("  IIT-Bombay!!!  ", "  SDE  ", 9)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("tribe_iit_bombay", state.tribeId)
        assertEquals("IIT-Bombay!!!", state.college)
        assertEquals("SDE", state.goal)
        assertEquals(7, state.podSize)
        assertTrue(state.isInTribe)
        assertNull(state.error)

        runBlocking {
            verify(userPrefs).setUserTribeId("tribe_iit_bombay")
            verify(userPrefs).setUserCollege("IIT-Bombay!!!")
            verify(userPrefs).saveTribeSetup("  SDE  ", 9)
        }
        verify(analytics).trackTribeJoined("IIT-Bombay!!!", 9)
    }

    @Test
    fun `tribe id is capped so the Firestore document stays bounded`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.createOrJoinTribe("A".repeat(60), "SDE", 5)
        advanceUntilIdle()

        assertEquals("tribe_" + "a".repeat(48), viewModel.uiState.value.tribeId)
    }

    @Test
    fun `leave clears tribe state and stored id`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.leaveTribe()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInTribe)
        assertEquals("", state.tribeId)
        assertNull(state.cohort)
        assertNull(state.error)
        runBlocking { verify(userPrefs).setUserTribeId("") }
    }

    @Test
    fun `cohort lookup failure degrades to no cohort instead of crashing`() = runTest(testDispatcher) {
        whenever(userPrefs.userCollege).thenReturn(flowOf("Offline College"))
        whenever(cohortRepository.getByCollege("Offline College")).thenReturn(
            flow { throw IllegalStateException("network down") }
        )

        val viewModel = viewModel()
        viewModel.loadTribe()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Offline College", state.college)
        assertNull(state.cohort)
    }

    @Test
    fun `blank college on load never queries the cohort repository`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.loadTribe()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isInTribe)
        verify(cohortRepository, never()).getByCollege(any())
    }
}
