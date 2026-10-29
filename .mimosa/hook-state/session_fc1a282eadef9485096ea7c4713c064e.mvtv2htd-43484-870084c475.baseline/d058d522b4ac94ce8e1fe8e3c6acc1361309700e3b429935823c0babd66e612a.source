package com.nexus.tribe

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.domain.model.CohortEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TribeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: TribeUiState,
        onBack: () -> Unit = {},
        onSetup: (String, String, Int) -> Unit = { _, _, _ -> },
        onLeave: () -> Unit = {},
    ) {
        composeRule.setContent {
            NexusTheme(dynamicColor = false) {
                TribeContent(
                    uiState = state,
                    onBack = onBack,
                    onSetup = onSetup,
                    onLeave = onLeave,
                )
            }
        }
    }

    @Test
    fun joinCard_opensSetupFormAndCancelReturns() {
        setContent(TribeUiState(isLoading = false))

        composeRule.onNodeWithText("Join your Campus Tribe").assertIsDisplayed()
        composeRule.onNodeWithText("Create or Join Tribe").performClick()

        composeRule.onNodeWithText("Set Up Your Tribe").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()

        composeRule.onNodeWithText("Join your Campus Tribe").assertIsDisplayed()
    }

    @Test
    fun setupForm_submitsCollegeGoalAndPodSize() {
        var capturedCollege = ""
        var capturedGoal = ""
        var capturedSize = 0
        setContent(
            TribeUiState(isLoading = false),
            onSetup = { college, goal, size ->
                capturedCollege = college
                capturedGoal = goal
                capturedSize = size
            },
        )

        composeRule.onNodeWithText("Create or Join Tribe").performClick()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("IIT Delhi")
        composeRule.onNodeWithText("Create Tribe").performScrollTo().performClick()

        assertEquals("IIT Delhi", capturedCollege)
        assertEquals("Product company — 6 months", capturedGoal)
        assertEquals(5, capturedSize)
    }

    @Test
    fun inTribe_rendersHomeWithoutCohort() {
        setContent(
            TribeUiState(
                isLoading = false,
                isInTribe = true,
                college = "IIT Delhi",
                goal = "SDE at a product company",
                podSize = 6,
            ),
        )

        composeRule.onNodeWithText("Your Tribe").assertIsDisplayed()
        composeRule.onNodeWithText("IIT Delhi").assertIsDisplayed()
        composeRule.onNodeWithText("Goal: SDE at a product company").assertIsDisplayed()
        composeRule.onNodeWithText("Pod size: 6").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Peer benchmarks for IIT Delhi appear once enough students from your college " +
                "are on NEXUS. We never invent member counts.",
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Leave Tribe").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun inTribe_rendersCohortBenchmarks() {
        setContent(
            TribeUiState(
                isLoading = false,
                isInTribe = true,
                college = "IIT Delhi",
                cohort = CohortEntry(
                    totalStudents = 200,
                    placedCount = 150,
                    avgScore = 640f,
                    avgStreak = 4.2f,
                    topSkills = listOf("Kotlin", "DSA"),
                ),
            ),
        )

        composeRule.onNodeWithText("Campus Cohort").assertIsDisplayed()
        composeRule.onNodeWithText("Students").assertIsDisplayed()
        composeRule.onNodeWithText("200").assertIsDisplayed()
        composeRule.onNodeWithText("Placed").assertIsDisplayed()
        composeRule.onNodeWithText("150").assertIsDisplayed()
        composeRule.onNodeWithText("Placement").assertIsDisplayed()
        composeRule.onNodeWithText("75%").assertIsDisplayed()
        composeRule.onNodeWithText("Avg score").assertIsDisplayed()
        composeRule.onNodeWithText("640").assertIsDisplayed()
        composeRule.onNodeWithText("Avg streak").assertIsDisplayed()
        composeRule.onNodeWithText("4d").assertIsDisplayed()
        composeRule.onNodeWithText("Top skills: Kotlin, DSA").assertIsDisplayed()
    }

    @Test
    fun error_rendersMessageAboveJoinCard() {
        setContent(TribeUiState(isLoading = false, error = "Could not load tribe"))

        composeRule.onNodeWithText("Could not load tribe").assertIsDisplayed()
        composeRule.onNodeWithText("Join your Campus Tribe").assertIsDisplayed()
    }

    @Test
    fun leave_invokesCallback() {
        var left = false
        setContent(
            TribeUiState(isLoading = false, isInTribe = true, college = "IIT Delhi"),
            onLeave = { left = true },
        )

        composeRule.onNodeWithText("Leave Tribe").performScrollTo().performClick()

        assertTrue(left)
    }

    @Test
    fun back_invokesCallback() {
        var backPressed = false
        setContent(TribeUiState(isLoading = false), onBack = { backPressed = true })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backPressed)
    }
}
