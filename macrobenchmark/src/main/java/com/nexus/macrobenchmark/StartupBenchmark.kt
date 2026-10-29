package com.nexus.macrobenchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold-start regression gate for the shipped build.
 *
 * `UseIfAvailable` keeps this runnable on the non-minified benchmark variant
 * as well as builds that package the generated baseline profile; install the
 * signed release on the same device to measure the profile itself.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = rule.measureRepeated(
        packageName = "com.nexus.app",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.UseIfAvailable,
            warmupIterations = 1,
        ),
    ) {
        pressHome()
        startActivityAndWait()
    }
}
