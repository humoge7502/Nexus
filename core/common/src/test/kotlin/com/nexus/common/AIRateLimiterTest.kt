package com.nexus.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AIRateLimiterTest {

    private val testScope = TestScope()
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var rateLimiter: AIRateLimiter

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                File.createTempFile("ai_rate_limit_test", ".preferences_pb")
            }
        )
        rateLimiter = AIRateLimiter(dataStore)
    }

    @Test
    fun `free user can make up to 10 calls per day`() = testScope.runTest {
        repeat(10) {
            val result = rateLimiter.canMakeAICall(isPro = false)
            assertTrue(result is RateLimitResult.Allowed)
            rateLimiter.recordAICall()
        }
        val result = rateLimiter.canMakeAICall(isPro = false)
        assertTrue(result is RateLimitResult.LimitExceeded)
    }

    @Test
    fun `pro user has no daily limit`() = testScope.runTest {
        repeat(50) {
            val result = rateLimiter.canMakeAICall(isPro = true)
            assertTrue(result is RateLimitResult.Allowed)
            rateLimiter.recordAICall()
        }
        val result = rateLimiter.canMakeAICall(isPro = true)
        assertTrue(result is RateLimitResult.Allowed)
    }

    @Test
    fun `remaining count decreases with each call`() = testScope.runTest {
        rateLimiter.recordAICall()
        rateLimiter.recordAICall()
        val result = rateLimiter.canMakeAICall(isPro = false)
        assertTrue(result is RateLimitResult.Allowed)
        assertEquals(8, (result as RateLimitResult.Allowed).remaining)
    }

    @Test
    fun `limit exceeded after max calls`() = testScope.runTest {
        repeat(10) { rateLimiter.recordAICall() }
        val result = rateLimiter.canMakeAICall(isPro = false)
        assertTrue(result is RateLimitResult.LimitExceeded)
        assertEquals(10, (result as RateLimitResult.LimitExceeded).callsToday)
    }
}
