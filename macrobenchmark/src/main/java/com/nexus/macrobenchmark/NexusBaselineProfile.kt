package com.nexus.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Collects a baseline profile for NEXUS.
 *
 * The app has no stable testTag hooks yet, so this walks the real UI using
 * the visible bottom-navigation labels. Every lookup is optional: profile
 * collection should still succeed if a screen renders slower than the wait
 * window instead of failing the whole run.
 */
@RunWith(AndroidJUnit4::class)
class NexusBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.nexus.app",
        profileBlock = {
            pressHome()
            startActivityAndWait()

            // Fresh installs start on auth/onboarding; skip both if present.
            repeat(2) {
                device.wait(Until.hasObject(By.text("Skip")), 3_000)
                    ?.let { device.findObject(By.text("Skip"))?.click() }
                device.waitForIdle()
            }

            device.wait(Until.hasObject(By.text("Home")), 10_000)

            fun openTab(label: String) {
                device.findObject(By.text(label))?.click()
                device.waitForIdle()
                repeat(2) {
                    device.swipe(540, 1500, 540, 600, 8)
                    device.waitForIdle()
                }
            }

            openTab("Home")
            openTab("Explore")
            openTab("AI")
            openTab("Growth")
            openTab("Me")
        }
    )
}
