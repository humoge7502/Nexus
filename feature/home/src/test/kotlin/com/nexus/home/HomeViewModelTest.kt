package com.nexus.home

import com.nexus.common.ApiResult
import com.nexus.data.repository.InternshipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.getAllInternships()).thenReturn(flowOf(emptyList()))
        whenever(repository.refreshInternships()).thenReturn(ApiResult.Success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state exposes ui state immediately`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository)
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun `loading completes after sample data collection`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `hardcoded stats are seeded until real data lands`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        assertEquals(23, viewModel.uiState.value.stats.totalApplications)
    }
}
