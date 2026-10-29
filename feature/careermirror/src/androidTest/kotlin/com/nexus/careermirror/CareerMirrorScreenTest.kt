package com.nexus.careermirror

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.domain.model.CareerMirror
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CareerMirrorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(state: CareerMirrorUiState, onBack: () -> Unit = {}) {
        composeRule.setContent {
            NexusTheme(dynamicColor = false) {
                CareerMirrorContent(uiState = state, onBack = onBack)
            }
        }
    }

    @Test
    fun loading_showsGeneratingMessage() {
        setContent(CareerMirrorUiState())

        composeRule.onNodeWithText("Generating your Career Mirror...").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsNotEnoughData() {
        setContent(CareerMirrorUiState(isLoading = false))

        composeRule.onNodeWithText("Not enough data yet").assertIsDisplayed()
        composeRule.onNodeWithText("Use Ascent for 90 days to generate your first mirror")
            .assertIsDisplayed()
    }

    @Test
    fun content_rendersPeriodScoreNarrativeInsightsAndActions() {
        setContent(
            CareerMirrorUiState(
                isLoading = false,
                mirror = CareerMirror(
                    periodLabel = "October 2026",
                    narrative = "A strong quarter of consistent practice.",
                    keyInsights = listOf("Ship more portfolio projects"),
                    actionItems = listOf("Apply to 10 product roles"),
                ),
                currentScore = 720,
                scoreDelta = 45,
            ),
        )

        composeRule.onNodeWithText("October 2026").assertIsDisplayed()
        composeRule.onNodeWithText("Score: 720 (+45)").assertIsDisplayed()
        composeRule.onNodeWithText("A strong quarter of consistent practice.").assertIsDisplayed()
        composeRule.onNodeWithText("Key Insights").assertIsDisplayed()
        composeRule.onNodeWithText("Ship more portfolio projects").assertIsDisplayed()
        composeRule.onNodeWithText("Next Quarter Actions").assertIsDisplayed()
        composeRule.onNodeWithText("Apply to 10 product roles").assertIsDisplayed()
    }

    @Test
    fun negativeDelta_rendersMinusSign() {
        setContent(
            CareerMirrorUiState(
                isLoading = false,
                mirror = CareerMirror(periodLabel = "September 2026", narrative = "A quieter month."),
                currentScore = 700,
                scoreDelta = -20,
            ),
        )

        composeRule.onNodeWithText("Score: 700 (-20)").assertIsDisplayed()
    }

    @Test
    fun back_invokesCallback() {
        var backPressed = false
        setContent(CareerMirrorUiState(isLoading = false), onBack = { backPressed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backPressed)
    }
}
