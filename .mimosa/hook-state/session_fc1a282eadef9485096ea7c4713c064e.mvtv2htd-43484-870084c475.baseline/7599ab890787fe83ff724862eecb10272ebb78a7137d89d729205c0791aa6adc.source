package com.nexus.placementforecast

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.domain.model.PlacementForecast
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlacementForecastScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: ForecastUiState,
        onBack: () -> Unit = {},
        onRefresh: () -> Unit = {},
        onWhatIfChange: (Int) -> Unit = {},
    ) {
        composeRule.setContent {
            NexusTheme(dynamicColor = false) {
                PlacementForecastContent(
                    uiState = state,
                    onBack = onBack,
                    onRefresh = onRefresh,
                    onWhatIfChange = onWhatIfChange,
                )
            }
        }
    }

    @Test
    fun errorState_rendersMessage() {
        setContent(ForecastUiState(isLoading = false, error = "Forecast unavailable"))

        composeRule.onNodeWithText("Forecast unavailable").assertIsDisplayed()
    }

    @Test
    fun emptyState_rendersGuidanceCopy() {
        setContent(ForecastUiState(isLoading = false))

        composeRule.onNodeWithText(
            "Not enough activity yet. Solve problems, log applications, and refresh to build a forecast.",
        ).assertIsDisplayed()
    }

    @Test
    fun content_rendersReadinessWhatIfAndForecastRows() {
        setContent(
            ForecastUiState(
                isLoading = false,
                currentScore = 720,
                modelReadinessPct = 68,
                modelFactors = listOf("Mock interviews"),
                modelVersion = "1.0.0",
                whatIfMocks = 4,
                whatIfPct = 74,
                forecasts = listOf(
                    PlacementForecast(
                        id = "tier-1",
                        companyTier = "Tier 1",
                        probabilityScore = 0.75f,
                        determiningFactors = listOf("DSA consistency"),
                    ),
                ),
            ),
        )

        composeRule.onNodeWithText("Current NEXUS Score: 720").assertIsDisplayed()
        composeRule.onNodeWithText("Placement readiness").assertIsDisplayed()
        composeRule.onNodeWithText("68% predicted probability of placement this season")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Top drivers:").assertIsDisplayed()
        composeRule.onNodeWithText("• Mock interviews").assertIsDisplayed()
        composeRule.onNodeWithText("What if I add 4 mock interviews?").assertIsDisplayed()
        composeRule.onNodeWithText("Readiness would move to 74% (+6 pts)").assertIsDisplayed()
        composeRule.onNodeWithText(
            "On-device model v1.0.0 · explainable, no data leaves your device",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Tier: Tier 1").assertIsDisplayed()
        composeRule.onNodeWithText("Probability: 75%").assertIsDisplayed()
        composeRule.onNodeWithText("• DSA consistency").assertIsDisplayed()
    }

    @Test
    fun refresh_invokesCallback() {
        var refreshed = false
        setContent(
            ForecastUiState(isLoading = false, forecasts = listOf(PlacementForecast(id = "one"))),
            onRefresh = { refreshed = true },
        )

        composeRule.onNodeWithContentDescription("Recalculate forecast").performClick()

        assertTrue(refreshed)
    }

    @Test
    fun back_invokesCallback() {
        var backPressed = false
        setContent(ForecastUiState(isLoading = false), onBack = { backPressed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backPressed)
    }
}
