package com.nexus.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AIRateLimiterTest {

    // DataStore needs an eager, independently-owned scope: with the test
    // scope itself its actor coroutine never completes (UncompletedCoroutinesError)
    // and buffered writes don't flush before assertions.
    private val dispatcher = UnconfinedTestDispatcher()
    private val storeScope = CoroutineScope(dispatcher + Job())
    private val testScope = TestScope(dispatcher)
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var rateLimiter: AIRateLimiter

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = storeScope,
            produceFile = {
                File.createTempFile("ai_rate_limit_test_${System.nanoTime()}", ".preferences_pb")
            }
        )
        rateLimiter = AIRateLimiter(dataStore)
    }

    @After
    fun tearDown() {
        storeScope.cancel()
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
