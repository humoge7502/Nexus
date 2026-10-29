package com.nexus.macrobenchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Scroll jank gate for the opportunity feed.
 *
 * Frame timing is only meaningful on a physical device; the emulator run
 * proves the harness and records a loose baseline.
 */
@RunWith(AndroidJUnit4::class)
class FrameTimingBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollExploreFeed() = rule.measureRepeated(
        packageName = "com.nexus.app",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.UseIfAvailable,
            warmupIterations = 1,
        ),
    ) {
        pressHome()
        startActivityAndWait()

        // Fresh installs start on auth/onboarding; skip both if present.
        repeat(2) {
            device.wait(Until.hasObject(By.text("Skip")), 2_000)
                ?.let { device.findObject(By.text("Skip"))?.click() }
            device.waitForIdle()
        }

        device.wait(Until.hasObject(By.desc("Explore")), 10_000)
        device.findObject(By.desc("Explore"))?.click()
        device.waitForIdle()

        repeat(5) {
            device.swipe(540, 1800, 540, 500, 20)
            device.waitForIdle()
        }
    }
}
