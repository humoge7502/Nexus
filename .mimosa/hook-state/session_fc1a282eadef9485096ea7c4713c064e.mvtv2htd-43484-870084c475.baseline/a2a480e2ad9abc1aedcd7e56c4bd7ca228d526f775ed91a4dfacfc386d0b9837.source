package com.nexus.analytics

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NexusFeatureFlagsTest {

    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val mockRemoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private lateinit var featureFlags: NexusFeatureFlags

    @Before
    fun setUp() {
        mockkStatic(FirebaseRemoteConfig::class)
        every { FirebaseRemoteConfig.getInstance() } returns mockRemoteConfig
        every { mockRemoteConfig.getString(FeatureFlagKeys.ONBOARDING_STEPS) } returns "3"
        every { mockRemoteConfig.getString(FeatureFlagKeys.HOME_LAYOUT) } returns "dashboard"
        every { mockRemoteConfig.getBoolean(FeatureFlagKeys.HOME_SHOW_INSIGHT_CARD) } returns true
        every { mockRemoteConfig.getBoolean(FeatureFlagKeys.ENABLE_RAG) } returns true
        every { mockRemoteConfig.getBoolean(FeatureFlagKeys.ENABLE_PROACTIVE_INSIGHTS) } returns true
        every { mockRemoteConfig.getString(FeatureFlagKeys.BADGE_CELEBRATION_STYLE) } returns "dialog"
        every { mockRemoteConfig.getLong(FeatureFlagKeys.AI_RATE_LIMIT_FREE) } returns 10L
        every { mockRemoteConfig.getLong(FeatureFlagKeys.NOTIFICATION_MORNING_TIME) } returns 7L
        every { mockRemoteConfig.getLong(FeatureFlagKeys.NOTIFICATION_EVENING_TIME) } returns 21L
        featureFlags = NexusFeatureFlags(analyticsTracker)
    }

    @Test
    fun `default onboarding steps is 3`() {
        assertEquals(3, featureFlags.onboardingSteps)
    }

    @Test
    fun `default home layout is dashboard`() {
        assertEquals(HomeLayout.DASHBOARD, featureFlags.homeLayout)
    }

    @Test
    fun `default insight card is shown`() {
        assertTrue(featureFlags.showInsightCard)
    }

    @Test
    fun `default RAG is enabled`() {
        assertTrue(featureFlags.isRAGEnabled)
    }

    @Test
    fun `default proactive insights are enabled`() {
        assertTrue(featureFlags.isProactiveInsightsEnabled)
    }

    @Test
    fun `default badge celebration style is dialog`() {
        assertEquals(CelebrationStyle.DIALOG, featureFlags.badgeCelebrationStyle)
    }

    @Test
    fun `trackExperimentExposure logs analytics event`() {
        every { mockRemoteConfig.getString("test_experiment") } returns "control"

        featureFlags.trackExperimentExposure("test_experiment")

        verify { analyticsTracker.track("experiment_exposure", mapOf("experiment" to "test_experiment", "variant" to "control")) }
    }

    @Test
    fun `default ai rate limit is 10`() {
        assertEquals(10, featureFlags.aiRateLimitFree)
    }

    @Test
    fun `default morning notification hour is 7`() {
        assertEquals(7, featureFlags.morningNotificationHour)
    }

    @Test
    fun `default evening notification hour is 21`() {
        assertEquals(21, featureFlags.eveningNotificationHour)
    }
}
