package com.nexus.ai

import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.datastore.UserPreferences
import com.nexus.domain.usecase.InterviewIntelligenceEngine
import com.nexus.network.ai.AIOpenRouterService
import com.nexus.network.ai.AIServiceException
import com.nexus.network.service.FirebaseFunctionsAIService
import com.nexus.network.service.RequiresSignInException
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * The AI tools share one rule: a call that never landed is a fault the
 * screen has to show, not a blank card that reads as an answer of "nothing".
 * These tests hold that line, including the two states an emulator without
 * outbound network cannot reach — an accepted report and the daily cap.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiToolOutcomeTest {

    private val dispatcher = StandardTestDispatcher()
    private val aiService: AIOpenRouterService = mock()
    private val rateLimiter: AIRateLimiter = mock()
    private val userPreferences: UserPreferences = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(userPreferences.isPro).thenReturn(flowOf(false))
        runBlocking { allowThreeMoreCalls() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun allowThreeMoreCalls() {
        whenever(rateLimiter.canMakeCall(any())).thenReturn(RateLimitResult.Allowed(remaining = 3))
    }

    private suspend fun capReached() {
        whenever(rateLimiter.canMakeCall(any()))
            .thenReturn(RateLimitResult.LimitExceeded(limit = 10, isPro = false, callsToday = 10))
    }

    private suspend fun replyWith(text: String) {
        whenever(aiService.sendMessage(any<String>())).thenReturn(text)
    }

    private suspend fun failWith(cause: Throwable) {
        // thenThrow() rejects AIServiceException because Kotlin emits no throws
        // clause; the answer lambda raises the same type the service does.
        whenever(aiService.sendMessage(any<String>())).thenAnswer { throw cause }
    }

    private suspend fun replyWithReport(text: String) {
        whenever(aiService.sendMessage(any<List<AIOpenRouterService.ChatMessage>>(), anyOrNull()))
            .thenReturn(text)
    }

    @Test
    fun `skill gap stops at the cap and says so`() = runTest(dispatcher) {
        capReached()
        val viewModel = SkillGapViewModel(aiService, rateLimiter, userPreferences)
        viewModel.updateCurrentSkills("Kotlin, Compose, SQL")
        viewModel.updateTargetRole("Android Engineer")

        viewModel.analyze()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLimitReached)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.analysis.isBlank())
    }

    @Test
    fun `skill gap reports a fault instead of an empty card`() = runTest(dispatcher) {
        failWith(AIServiceException("Could not reach the AI service"))
        val viewModel = SkillGapViewModel(aiService, rateLimiter, userPreferences)
        viewModel.updateCurrentSkills("Kotlin, Compose, SQL")
        viewModel.updateTargetRole("Android Engineer")

        viewModel.analyze()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.analysis.isBlank())
        assertEquals("Could not reach the AI service", state.error)
    }

    @Test
    fun `coach retry reruns the call that failed, not the one that worked`() = runTest(dispatcher) {
        val intelligence: InterviewIntelligenceEngine = mock()
        val viewModel = InterviewCoachViewModel(aiService, rateLimiter, userPreferences, intelligence)
        viewModel.updateTargetRole("Backend Engineer")

        replyWith("Tell me about a system you designed.")
        viewModel.generateQuestion()
        advanceUntilIdle()
        viewModel.updateAnswer("I split the monolith into three services.")

        failWith(AIServiceException("Timeout reading the AI service"))
        viewModel.getFeedback()
        advanceUntilIdle()

        val failed = viewModel.uiState.value
        assertEquals(CoachOperation.Feedback, failed.failedOperation)
        assertEquals("Timeout reading the AI service", failed.error)
        assertEquals("I split the monolith into three services.", failed.userAnswer)

        val prompts = argumentCaptor<String>()
        replyWith("Good structure. Add the numbers.")
        viewModel.retry()
        advanceUntilIdle()

        verify(aiService, atLeastOnce()).sendMessage(prompts.capture())
        assertTrue(prompts.firstValue.contains("Backend Engineer"))
        assertTrue(prompts.lastValue.contains("I split the monolith into three services."))
        assertEquals("Tell me about a system you designed.", viewModel.uiState.value.question)
        assertEquals("Good structure. Add the numbers.", viewModel.uiState.value.feedback)
    }

    @Test
    fun `career insights shows the fault on open rather than a quiet screen`() = runTest(dispatcher) {
        failWith(AIServiceException("No network"))
        val viewModel = CareerInsightsViewModel(aiService, rateLimiter, userPreferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("No network", state.error)
        assertTrue(state.insights.isBlank())
    }

    @Test
    fun `linkedin draft stops at the cap`() = runTest(dispatcher) {
        capReached()
        val viewModel = LinkedInPostViewModel(aiService, rateLimiter, userPreferences)
        viewModel.updateAchievement("Won a 200-person hackathon")

        viewModel.generate()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLimitReached)
        assertTrue(viewModel.uiState.value.generatedPost.isBlank())
    }

    @Test
    fun `resume refuses to score a fragment`() = runTest(dispatcher) {
        val viewModel = ResumeAnalyzerViewModel(aiService, ResumeAnalysisParser(), rateLimiter, userPreferences)
        viewModel.updateResumeText("Kotlin developer. One project.")

        viewModel.analyze()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isTooShort)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resume scores the report the model actually returned`() = runTest(dispatcher) {
        replyWithReport(
            """
            Here is the assessment:
            {"ats_score": 72, "sections": [{"name": "Experience", "score": 80, "note": "Roles are dated."},
             {"name": "Education", "score": 60, "note": "No percentage line."}],
             "missing_keywords": ["Docker", "CI/CD"], "recommendations": ["Quantify the internship impact."]}
            """.trimIndent(),
        )
        val viewModel = ResumeAnalyzerViewModel(aiService, ResumeAnalysisParser(), rateLimiter, userPreferences)
        viewModel.updateResumeText("SAGITTA ".repeat(60))

        viewModel.analyze()
        advanceUntilIdle()

        val analysis = checkNotNull(viewModel.uiState.value.analysis)
        assertEquals(72, analysis.atsScore)
        assertEquals(2, analysis.sections.size)
        assertEquals(listOf("Docker", "CI/CD"), analysis.missingKeywords)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resume keeps an unreadable reply as a fault`() = runTest(dispatcher) {
        replyWithReport("I could not assess this resume.")
        val viewModel = ResumeAnalyzerViewModel(aiService, ResumeAnalysisParser(), rateLimiter, userPreferences)
        viewModel.updateResumeText("SAGITTA ".repeat(60))

        viewModel.analyze()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.analysis)
        assertNotNull(state.error)
    }

    @Test
    fun `roadmap stops at the cap without calling the service`() = runTest(dispatcher) {
        capReached()
        val viewModel = roadmapViewModel()
        viewModel.updateRole("Backend Engineer")

        viewModel.generate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLimitReached)
        assertFalse(state.isLoading)
        // The cap is not a fault: keeping it out of `error` is what lets the
        // screen show the Pro route instead of a Retry that cannot work.
        assertNull(state.error)
        verify(aiService, never()).sendMessage(any<String>())
    }

    @Test
    fun `roadmap keeps the role when the call never lands`() = runTest(dispatcher) {
        failWith(AIServiceException("OpenRouter returned 503"))
        val viewModel = roadmapViewModel()
        viewModel.updateRole("Data Analyst")

        viewModel.generate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLimitReached)
        assertEquals("OpenRouter returned 503", state.error)
        assertEquals("Data Analyst", state.targetRole)
        assertTrue(state.roadmap.isBlank())
    }

    private fun roadmapViewModel() = RoadmapViewModel(
        aiService,
        rateLimiter,
        mock(),
        mock(),
        userPreferences,
    )

    @Test
    fun `research treats a missing account as a precondition, not a dropped request`() =
        runTest(dispatcher) {
            val service: FirebaseFunctionsAIService = mock()
            whenever(service.deepResearch(any(), any()))
                .thenReturn(Result.failure(RequiresSignInException("Sign in to use Deep Research")))
            val viewModel = ResearchViewModel(service)

            viewModel.research("Which skills move a data analyst into a data science role?")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.requiresSignIn)
            assertFalse(state.isLoading)
        }

    @Test
    fun `research leaves a transport failure retryable`() = runTest(dispatcher) {
        val service: FirebaseFunctionsAIService = mock()
        whenever(service.deepResearch(any(), any()))
            .thenReturn(Result.failure(IllegalStateException("Empty research response")))
        val viewModel = ResearchViewModel(service)

        viewModel.research("What do product companies test in interviews?")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.requiresSignIn)
        assertEquals("Empty research response", state.error)
    }
}
