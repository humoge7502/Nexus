package com.nexus.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NexusBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.nexus.careerOS",
        profileBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.desc("nexus_home_content")), 5_000)

            device.findObject(By.desc("Explore"))?.click()
            device.wait(Until.hasObject(By.res("explore_screen")), 3_000)
            repeat(3) {
                device.swipe(540, 1200, 540, 400, 10)
                Thread.sleep(500)
            }
            device.findObjects(By.res("opportunity_card")).firstOrNull()?.click()
            device.wait(Until.hasObject(By.res("apply_button")), 3_000)
            device.pressBack()

            device.findObject(By.desc("AI"))?.click()
            device.wait(Until.hasObject(By.res("ai_hub_screen")), 3_000)
            device.findObject(By.text("AI Mentor"))?.click()
            device.wait(Until.hasObject(By.res("ai_chat_input")), 3_000)
            device.pressBack()

            device.findObject(By.desc("Growth"))?.click()
            device.wait(Until.hasObject(By.res("growth_dashboard")), 3_000)
            device.swipe(540, 1200, 540, 400, 10)

            device.findObject(By.desc("Me"))?.click()
            device.wait(Until.hasObject(By.res("profile_screen")), 3_000)
        }
    )
}
