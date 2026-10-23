package com.nexus.data.usecase

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.UserSurveyDao
import com.nexus.datastore.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SurveyManagerTest {

    private val surveyDao: UserSurveyDao = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private lateinit var surveyManager: SurveyManager

    @Before
    fun setUp() {
        surveyManager = SurveyManager(surveyDao, userPreferences, analyticsTracker)
    }

    @Test
    fun `evaluateSurveyEligibility returns null for new user`() = runTest {
        coEvery { userPreferences.lastLoginDate } returns flowOf("")

        val survey = surveyManager.evaluateSurveyEligibility(daysActive = 0, sessionCount = 0)

        assertNull(survey)
    }

    @Test
    fun `evaluateSurveyEligibility returns null before day 7 with few sessions`() = runTest {
        coEvery { userPreferences.lastLoginDate } returns flowOf("")

        val survey = surveyManager.evaluateSurveyEligibility(daysActive = 5, sessionCount = 2)

        assertNull(survey)
    }

    @Test
    fun `evaluateSurveyEligibility returns SeanEllis at day 7 with 5 sessions`() = runTest {
        coEvery { userPreferences.lastLoginDate } returns flowOf("")

        val survey = surveyManager.evaluateSurveyEligibility(daysActive = 7, sessionCount = 5)

        assertTrue(survey is SurveyPrompt.SeanEllis)
    }

    @Test
    fun `evaluateSurveyEligibility returns NPS at day 14 regardless of sessions`() = runTest {
        coEvery { userPreferences.lastLoginDate } returns flowOf("")

        val survey = surveyManager.evaluateSurveyEligibility(daysActive = 14, sessionCount = 0)

        assertTrue(survey is SurveyPrompt.NPS)
    }

    @Test
    fun `evaluateSurveyEligibility returns SeanEllis at day 21 even with zero sessions`() = runTest {
        coEvery { userPreferences.lastLoginDate } returns flowOf("")

        val survey = surveyManager.evaluateSurveyEligibility(daysActive = 21, sessionCount = 0)

        assertTrue(survey is SurveyPrompt.SeanEllis)
    }

    @Test
    fun `evaluateSurveyEligibility returns null when lastSurveyKnown`() = runTest {
        coEvery { userPreferences.lastLoginDate } returns flowOf("2026-05-01")

        val survey = surveyManager.evaluateSurveyEligibility(daysActive = 21, sessionCount = 10)

        assertNull(survey)
    }

    @Test
    fun `recordResponse inserts survey entity`() = runTest {
        surveyManager.recordResponse(
            surveyType = "SEAN_ELLIS",
            score = 9,
            responseText = "Very useful",
            triggerEvent = "session_start_day_7",
            daysActive = 7,
        )

        coVerify(exactly = 1) { surveyDao.insert(any()) }
    }

    @Test
    fun `recordResponse tracks analytics for Sean Ellis response`() = runTest {
        surveyManager.recordResponse(
            surveyType = "SEAN_ELLIS",
            score = 9,
            responseText = null,
            triggerEvent = "session_start_day_7",
            daysActive = 7,
        )

        coVerify { analyticsTracker.track("sean_ellis_response", any()) }
    }

    @Test
    fun `recordResponse tracks analytics for NPS response`() = runTest {
        surveyManager.recordResponse(
            surveyType = "NPS",
            score = 8,
            responseText = "Good app",
            triggerEvent = "session_start_day_14",
            daysActive = 14,
        )

        coVerify { analyticsTracker.track("nps_response", any()) }
    }
}
