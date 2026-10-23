package com.nexus.ai

import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.AIRateLimiter
import com.nexus.data.repository.AIConversationRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.data.usecase.CareerMemoryManager
import com.nexus.datastore.UserPreferences
import com.nexus.domain.repository.ISkillGraphRepository
import com.nexus.domain.usecase.AIResponseValidator
import com.nexus.domain.usecase.ConversationMemoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class AIChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AIConversationRepository

    private val nexusAIService: NexusAIService = mock()
    private val rateLimiter: AIRateLimiter = mock()
    private val userPreferences: UserPreferences = mock()
    private val xpRepository: UserXpRepository = mock()
    private val analyticsTracker: AnalyticsTracker = mock()
    private val memoryManager: ConversationMemoryManager = mock()
    private val aiResponseValidator: AIResponseValidator = mock()
    private val careerMemoryManager: CareerMemoryManager = mock()
    private val skillGraphRepository: ISkillGraphRepository = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.getAllConversations()).thenReturn(flowOf(emptyList()))
        whenever(repository.createConversation(org.mockito.kotlin.any())).thenReturn("conv_1")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AIChatViewModel(
        repository = repository,
        nexusAIService = nexusAIService,
        rateLimiter = rateLimiter,
        userPreferences = userPreferences,
        xpRepository = xpRepository,
        analyticsTracker = analyticsTracker,
        memoryManager = memoryManager,
        aiResponseValidator = aiResponseValidator,
        careerMemoryManager = careerMemoryManager,
        skillGraphRepository = skillGraphRepository,
    )

    @Test
    fun `initial state exposes empty conversations`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value)
        assertEquals(emptyList<Any>(), viewModel.uiState.value.conversations)
    }

    @Test
    fun `createConversation seeds greeting message`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.createConversation("Test Chat")
        advanceUntilIdle()

        assertEquals("conv_1", viewModel.uiState.value.currentConversationId)
        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals(false, viewModel.uiState.value.messages.first().isUser)
    }
}
