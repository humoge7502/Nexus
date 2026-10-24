package com.nexus.home

import com.nexus.common.ApiResult
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
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
import org.mockito.kotlin.whenever

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: InternshipRepository
    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var userPreferences: UserPreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        applicationRepository = mock()
        userPreferences = mock()
        whenever(repository.getAllInternships()).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(repository.refreshInternships()).thenReturn(ApiResult.Success(emptyList()))
        }
        whenever(applicationRepository.getAllApplications()).thenReturn(flowOf(emptyList()))
        whenever(userPreferences.displayName).thenReturn(flowOf("Test User"))
        whenever(userPreferences.studyStreak).thenReturn(flowOf(7))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state exposes ui state immediately`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository, applicationRepository, userPreferences)
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun `real display name replaces default`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository, applicationRepository, userPreferences)
        advanceUntilIdle()
        assertEquals("Test User", viewModel.uiState.value.userName)
    }

    @Test
    fun `streak comes from preferences`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository, applicationRepository, userPreferences)
        advanceUntilIdle()
        assertEquals(7, viewModel.uiState.value.streakDays)
    }

    @Test
    fun `blank name falls back to aspirational default`() = runTest(testDispatcher) {
        whenever(userPreferences.displayName).thenReturn(flowOf(""))
        val viewModel = HomeViewModel(repository, applicationRepository, userPreferences)
        advanceUntilIdle()
        assertEquals("Future Engineer", viewModel.uiState.value.userName)
    }
}
