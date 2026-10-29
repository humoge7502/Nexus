package com.nexus.community

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.domain.model.Hackathon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CommunityScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: CommunityUiState,
        onOpenCohort: () -> Unit = {},
        onOpenTribe: () -> Unit = {},
        onOpenHackathon: (String) -> Unit = {},
        onRefresh: () -> Unit = {},
    ) {
        composeRule.setContent {
            NexusTheme(dynamicColor = false) {
                CommunityContent(
                    uiState = state,
                    onOpenCohort = onOpenCohort,
                    onOpenTribe = onOpenTribe,
                    onOpenHackathon = onOpenHackathon,
                    onRefresh = onRefresh,
                )
            }
        }
    }

    @Test
    fun content_rendersBatchHackathonsAndActions() {
        var openedHackathon: String? = null
        var openedCohort = false
        var openedTribe = false
        setContent(
            state = CommunityUiState(
                isLoading = false,
                college = "IIT Bombay",
                hackathons = listOf(hackathon("h1", isLive = true)),
            ),
            onOpenCohort = { openedCohort = true },
            onOpenTribe = { openedTribe = true },
            onOpenHackathon = { openedHackathon = it },
        )

        composeRule.onNodeWithText("College: IIT Bombay").assertIsDisplayed()
        composeRule.onNodeWithText("Hackathon h1").assertIsDisplayed()
        composeRule.onNodeWithText("Live").assertIsDisplayed()

        composeRule.onNodeWithText("Open cohort intelligence").performClick()
        composeRule.onNodeWithText("Open Tribe").performClick()
        composeRule.onNodeWithText("View details").performClick()

        assertTrue(openedCohort)
        assertTrue(openedTribe)
        assertEquals("h1", openedHackathon)
    }

    @Test
    fun emptyHackathons_showsHintAndRefreshCallback() {
        var refreshed = false
        setContent(
            state = CommunityUiState(isLoading = false, college = ""),
            onRefresh = { refreshed = true },
        )

        composeRule
            .onNodeWithText("Add your college in onboarding", substring = true)
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("No open hackathons right now", substring = true)
            .assertIsDisplayed()

        composeRule.onNodeWithText("Refresh").performClick()
        assertTrue(refreshed)
    }

    private fun hackathon(id: String, isLive: Boolean) = Hackathon(
        id = id,
        title = "Hackathon $id",
        organizer = "Organizer",
        startDate = LocalDate.of(2026, 10, 28),
        endDate = LocalDate.of(2026, 10, 30),
        registrationDeadline = LocalDate.of(2026, 11, 5),
        isLive = isLive,
    )
}
