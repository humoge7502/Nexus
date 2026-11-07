package com.nexus.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
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

    @Test(timeout = 180_000)
    fun everyStation_opensWithoutCrashing() {
        dismissBootFlowIfPresent()
        dismissTourIfPresent()

        listOf(
            DesignR.string.nav_explore,
            DesignR.string.nav_ai,
            DesignR.string.nav_growth,
            DesignR.string.nav_profile,
            DesignR.string.nav_home,
        ).forEach { labelRes ->
            val label = composeRule.activity.getString(labelRes)
            waitAndClick(label)
        }
    }

    /** One node lookup that tolerates an empty hierarchy (unknown current step). */
    private fun hasTextOrEmpty(text: String): Boolean = try {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    } catch (_: Exception) {
        false
    }

    /**
     * Polls for [text] and clicks it once it appears. The boot flow swaps
     * auth/onboarding/main asynchronously from StateFlows, so an immediate
     * probe can observe an empty (splash) composition that is about to be
     * replaced; waiting is what makes the walk deterministic.
     */
    private fun waitAndClick(text: String, attempts: Int = 40) {
        repeat(attempts) {
            if (hasTextOrEmpty(text)) {
                try {
                    composeRule.onAllNodesWithText(text).onFirst().performClick()
                    composeRule.waitForIdle()
                    return
                } catch (_: Exception) {
                    // The permission activity takes the foreground and removes
                    // the hierarchy; answer it and let the poll loop retry.
                    dismissPermissionIfPresent()
                }
                // Clicking a leaked hierarchy still throws below; if that
                // happened, re-enter the poll before the final throw so the
                // diagnostic names the label, not a transient window change.
                if (hasTextOrEmpty(text)) {
                    try {
                        composeRule.onAllNodesWithText(text).onFirst().performClick()
                        composeRule.waitForIdle()
                        return
                    } catch (_: Exception) {
                        dismissPermissionIfPresent()
                    }
                }
            }
            Thread.sleep(250)
        }
        // Final attempt; assertion error carries the selector for diagnosis.
        composeRule.onAllNodesWithText(text).onFirst().performClick()
        composeRule.waitForIdle()
    }

    /**
     * Back denies the system permission prompt and returns to the activity.
     * Pressing Back directly is enough for the composable helper; for the
     * launcher-style dialog the UiSelector path below finds the button.
     */
    private fun dismissPermissionIfPresent() {
        val device = try {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        } catch (_: Exception) {
            return
        }
        try {
            // pressBack on the app itself exits it and destroys the hierarchy
            // entirely, so only answer a prompt that is actually in the
            // foreground (from the permission controller, not from us).
            if (device.currentPackageName != "com.android.permissioncontroller") return
            val deny = device.findObject(By.text("DON'T ALLOW"))
            deny.click()
            device.wait(Until.hasObject(By.text("DON'T ALLOW")), 2_000)
        } catch (_: Exception) {
            // No dialog matched; the poll loop retries regardless.
        }
    }

    /** Mirrors [hasTextOrEmpty] but reveals which lookup the click failed on. */
    private fun clickIfPresent(text: String, attempts: Int = 6) {
        repeat(attempts) {
            if (hasTextOrEmpty(text)) {
                try {
                    composeRule.onAllNodesWithText(text).onFirst().performClick()
                    composeRule.waitForIdle()
                    return
                } catch (_: Exception) {
                    dismissPermissionIfPresent()
                }
                if (hasTextOrEmpty(text)) {
                    try {
                        composeRule.onAllNodesWithText(text).onFirst().performClick()
                        composeRule.waitForIdle()
                        return
                    } catch (_: Exception) {
                        dismissPermissionIfPresent()
                    }
                }
            }
            Thread.sleep(250)
        }
    }

    /** The tour scrim swallows taps meant for the bar underneath, so it goes first. */
    private fun dismissTourIfPresent() {
        clickIfPresent(composeRule.activity.getString(R.string.tour_skip))
    }

    /**
     * A fresh install lands on Auth, then Onboarding — neither has a bottom bar,
     * so the station walk below can only start once both are answered. The
     * guest path is the one the app offers and the one this test takes.
     */
    private fun dismissBootFlowIfPresent() {
        // Auth: "Continue without an account" is the no-account route.
        clickIfPresent(composeRule.activity.getString(com.nexus.auth.R.string.text_skip_for_now))
        // Onboarding: "Skip - see the catalog first" skips the whole pager.
        clickIfPresent(composeRule.activity.getString(com.nexus.onboarding.R.string.onboarding_skip))
    }
}