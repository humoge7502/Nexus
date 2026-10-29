package com.nexus.app

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Boots the real Hilt/Firebase/Room graph, which every feature test replaces with fakes. */
@RunWith(AndroidJUnit4::class)
class MainActivityBootTest {

    @Test(timeout = 60_000)
    fun coldStart_reachesResumedWithoutCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                assertEquals(Lifecycle.State.RESUMED, activity.lifecycle.currentState)
            }
        }
    }
}
