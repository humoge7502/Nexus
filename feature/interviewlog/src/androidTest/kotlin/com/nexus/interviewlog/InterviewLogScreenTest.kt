package com.nexus.interviewlog

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.domain.model.InterviewOutcome
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterviewLogScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(state: InterviewLogUiState, onBack: () -> Unit = {}) {
        composeRule.setContent {
            NexusTheme(dynamicColor = false) {
                InterviewLogContent(uiState = state, onBack = onBack)
            }
        }
    }

    @Test
    fun content_rendersStatisticsAndOutcomeRows() {
        setContent(
            InterviewLogUiState(
                isLoading = false,
                topCompanies = listOf("Google" to 3),
                outcomes = listOf(
                    InterviewOutcome(
                        company = "Google",
                        role = "SDE",
                        interviewRound = "Round 2",
                        result = "SELECTED",
                    ),
                ),
            ),
        )

        composeRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeRule.onNodeWithText("Google: 3 interviews").assertIsDisplayed()
        composeRule.onNodeWithText("Google").assertIsDisplayed()
        composeRule.onNodeWithText("Role: SDE").assertIsDisplayed()
        composeRule.onNodeWithText("Round: Round 2").assertIsDisplayed()
        composeRule.onNodeWithText("Result: SELECTED").assertIsDisplayed()
    }

    @Test
    fun emptyLog_keepsStatisticsCardWithoutOutcomeRows() {
        setContent(InterviewLogUiState(isLoading = false))

        composeRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeRule.onAllNodesWithText("Result:", substring = true).assertCountEquals(0)
    }

    @Test
    fun back_invokesCallback() {
        var backPressed = false
        setContent(InterviewLogUiState(isLoading = false), onBack = { backPressed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backPressed)
    }
}
