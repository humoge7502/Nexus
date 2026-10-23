package com.nexus.ai

import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.data.repository.AIConversationRepository
import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import com.nexus.domain.usecase.ConversationMemoryManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AIChatViewModelTest {

    @MockK
    private lateinit var repository: AIConversationRepository

    @MockK
    private lateinit var openRouterService: AIOpenRouterService

    @MockK
    private lateinit var rateLimiter: AIRateLimiter

    @MockK
    private lateinit var conversationMemoryManager: ConversationMemoryManager

    private lateinit var viewModel: AIChatViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllConversations() } returns flowOf(emptyList())
        coEvery { rateLimiter.canMakeCall(any()) } returns RateLimitResult.Allowed(remaining = 10)
        coEvery { rateLimiter.recordCall() } just runs
        viewModel = AIChatViewModel(repository, openRouterService, rateLimiter, conversationMemoryManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage adds user message and returns AI response`() = runTest(testDispatcher) {
        coEvery { openRouterService.sendMessage(any<List<AIOpenRouterService.ChatMessage>>()) } returns "AI response text"
        coEvery { repository.createConversation() } returns "conv_1"
        coEvery { repository.addMessage(any(), any()) } just runs

        viewModel.sendMessage("Hello")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.messages.size == 3) // ai_0 initial + user + AI
        assert(state.messages[1].text == "Hello")
        assert(state.messages[1].isUser)
        assert(state.messages[2].text == "AI response text")
        assert(!state.messages[2].isUser)
        assert(!state.isThinking)
    }

    @Test
    fun `sendMessage sets loading state during API call`() = runTest(testDispatcher) {
        coEvery { openRouterService.sendMessage(any<List<AIOpenRouterService.ChatMessage>>()) } coAnswers {
            delay(100)
            "response"
        }
        coEvery { repository.createConversation() } returns "conv_1"
        coEvery { repository.addMessage(any(), any()) } just runs

        viewModel.sendMessage("Hello")

        runCurrent()
        assert(viewModel.uiState.value.isThinking)

        advanceTimeBy(100)
        runCurrent()
        assert(!viewModel.uiState.value.isThinking)
        assert(viewModel.uiState.value.messages.size == 3)
    }

    @Test
    fun `sendMessage persists messages to repository`() = runTest(testDispatcher) {
        coEvery { openRouterService.sendMessage(any<List<AIOpenRouterService.ChatMessage>>()) } returns "AI reply"
        coEvery { repository.createConversation() } returns "conv_1"
        coEvery { repository.addMessage(any(), any()) } just runs

        viewModel.sendMessage("Hello")
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.addMessage(any(), any()) }
        coVerify { repository.addMessage("conv_1", match { it.role == AiMessageRole.USER && it.content == "Hello" }) }
        coVerify { repository.addMessage("conv_1", match { it.role == AiMessageRole.ASSISTANT && it.content == "AI reply" }) }
    }

    @Test
    fun `sendMessage fallback to mock when API throws`() = runTest(testDispatcher) {
        coEvery { openRouterService.sendMessage(any<List<AIOpenRouterService.ChatMessage>>()) } throws Exception("API down")
        coEvery { repository.createConversation() } returns "conv_1"
        coEvery { repository.addMessage(any(), any()) } just runs

        viewModel.sendMessage("interview tips")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.messages.size == 3)
        assert(state.messages[2].text.contains("interview", ignoreCase = true))
        assert(!state.isThinking)
    }
}
