package com.nexus.cohortintelligence

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.domain.model.CohortEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CohortIntelligenceScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: CohortUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeRule.setContent {
            NexusTheme(dynamicColor = false) {
                CohortIntelligenceContent(
                    uiState = state,
                    onBack = onBack,
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun content_rendersCollegePlacementRateAndCohortRows() {
        setContent(
            CohortUiState(
                isLoading = false,
                collegeId = "IIT Bombay",
                placementRate = 0.82f,
                entries = listOf(
                    CohortEntry(
                        semester = "2026",
                        companyTier = "Tier 1",
                        placedCount = 120,
                        totalStudents = 150,
                    ),
                ),
            ),
        )

        composeRule.onNodeWithText("College: IIT Bombay").assertIsDisplayed()
        composeRule.onNodeWithText("Estimated Placement Rate: 82%").assertIsDisplayed()
        composeRule.onNodeWithText("Semester: 2026").assertIsDisplayed()
        composeRule.onNodeWithText("Tier: Tier 1").assertIsDisplayed()
        composeRule.onNodeWithText("Placed students: 120 / 150").assertIsDisplayed()
    }

    @Test
    fun emptyCohort_namesTheAbsenceInsteadOfLeavingTheHeaderStandingAlone() {
        setContent(CohortUiState(isLoading = false, collegeId = "IIT Bombay"))

        composeRule.onNodeWithText("No cohort placements yet").assertIsDisplayed()
        composeRule.onAllNodesWithText("College: IIT Bombay").assertCountEquals(0)
        composeRule.onAllNodesWithText("Estimated Placement Rate", substring = true)
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Semester:", substring = true).assertCountEquals(0)
    }

    @Test
    fun missingCollege_saysSoRatherThanReportingAnEmptyCohort() {
        setContent(CohortUiState(isLoading = false, collegeId = ""))

        composeRule.onNodeWithText("No college on your profile").assertIsDisplayed()
        composeRule.onAllNodesWithText("No cohort placements yet").assertCountEquals(0)
    }

    @Test
    fun faultedRead_offersRetryInsteadOfSpinningForever() {
        var retries = 0
        setContent(
            CohortUiState(
                isLoading = false,
                collegeId = "IIT Bombay",
                error = "database connection failed",
            ),
            onRetry = { retries++ },
        )

        composeRule.onNodeWithText("Can't read your cohort").assertIsDisplayed()
        composeRule.onNodeWithText("database connection failed").assertIsDisplayed()
        composeRule.onNodeWithText("Try again").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun back_invokesCallback() {
        var backPressed = false
        setContent(CohortUiState(isLoading = false), onBack = { backPressed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backPressed)
    }
}
