package com.nexus.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.designsystem.R as DesignR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Walks every bottom-bar station in the real app with the real dependency graph.
 *
 * A view-model that blows up while its screen is being constructed takes the
 * process down, and cold-start alone never builds the other four — a Profile
 * init-order crash reached a device unnoticed by the rest of the suite.
 */
@RunWith(AndroidJUnit4::class)
class StationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test(timeout = 120_000)
    fun everyStation_opensWithoutCrashing() {
        composeRule.waitForIdle()
        dismissTourIfPresent()

        listOf(
            DesignR.string.nav_explore,
            DesignR.string.nav_ai,
            DesignR.string.nav_growth,
            DesignR.string.nav_profile,
            DesignR.string.nav_home,
        ).forEach { labelRes ->
            val label = composeRule.activity.getString(labelRes)
            composeRule.onAllNodesWithText(label).onFirst().performClick()
            composeRule.waitForIdle()
        }
    }

    /** The tour scrim swallows taps meant for the bar underneath, so it goes first. */
    private fun dismissTourIfPresent() {
        val skip = composeRule.activity.getString(R.string.tour_skip)
        if (composeRule.onAllNodesWithText(skip).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onAllNodesWithText(skip).onFirst().performClick()
            composeRule.waitForIdle()
        }
    }
}
