package com.nexus.community

import com.nexus.common.ApiResult
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Hackathon
import com.nexus.domain.repository.IOpportunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: IOpportunityRepository
    private lateinit var userPreferences: UserPreferences

    private val today = LocalDate.of(2026, 10, 27)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        userPreferences = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `upcoming hackathons exclude expired entries and put live events first`() {
        val expired = hackathon("expired", today.minusDays(1))
        val soon = hackathon("soon", today.plusDays(3))
        val live = hackathon("live", today.plusDays(5), isLive = true)
        val later = hackathon("later", today.plusDays(14))

        val result = listOf(expired, later, soon, live).upcomingForCommunity(today, limit = 4)

        assertEquals(listOf("live", "soon", "later"), result.map { it.id })
    }

    @Test
    fun `upcoming hackathons respect the limit`() {
        val items = (1..5).map { hackathon("h$it", today.plusDays(it.toLong())) }

        val result = items.upcomingForCommunity(today, limit = 2)

        assertEquals(2, result.size)
        assertEquals(listOf("h1", "h2"), result.map { it.id })
    }

    @Test
    fun `ui state exposes trimmed college and live hackathons`() = runTest {
        whenever(userPreferences.userCollege).thenReturn(flowOf("  IIT Bombay  "))
        whenever(repository.getHackathons()).thenReturn(
            flowOf(listOf(hackathon("live", LocalDate.now().plusDays(2), isLive = true))),
        )

        val viewModel = CommunityViewModel(repository, userPreferences)
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("IIT Bombay", state.college)
        assertEquals(listOf("live"), state.hackathons.map { it.id })
    }

    @Test
    fun `refresh delegates to the repository and clears the refreshing flag`() = runTest {
        whenever(repository.refreshOpportunities()).thenReturn(ApiResult.Success(Unit))
        whenever(userPreferences.userCollege).thenReturn(flowOf(""))
        whenever(repository.getHackathons()).thenReturn(flowOf(emptyList()))

        val viewModel = CommunityViewModel(repository, userPreferences)
        viewModel.refresh()
        advanceUntilIdle()

        verify(repository).refreshOpportunities()
        assertFalse(viewModel.isRefreshing.value)
    }

    private fun hackathon(id: String, deadline: LocalDate, isLive: Boolean = false) = Hackathon(
        id = id,
        title = "Hackathon $id",
        organizer = "Organizer",
        startDate = deadline.minusDays(2),
        endDate = deadline.plusDays(2),
        registrationDeadline = deadline,
        isLive = isLive,
    )
}
