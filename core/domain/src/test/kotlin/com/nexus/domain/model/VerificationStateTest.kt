package com.nexus.domain.model

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class VerificationStateTest {

    private val verified: Instant = Instant.parse("2026-11-08T06:00:00Z")
    private val now: Instant = Instant.parse("2026-11-08T12:00:00Z")

    @Test
    fun `null verification reads unverified`() {
        assertEquals(VerificationState.UNVERIFIED, VerificationPolicy.stateOf(null, now))
    }

    @Test
    fun `fresh check inside the quiet window reads verified`() {
        assertEquals(
            VerificationState.VERIFIED,
            VerificationPolicy.stateOf(now.minus(Duration.ofHours(47)), now),
        )
    }

    @Test
    fun `check exactly at the quiet window still reads verified`() {
        // The boundary is strict: the shipped bands speak only past 48 hours.
        assertEquals(
            VerificationState.VERIFIED,
            VerificationPolicy.stateOf(now.minus(Duration.ofHours(48)), now),
        )
    }

    @Test
    fun `aging check past the quiet window reads stale`() {
        assertEquals(
            VerificationState.STALE,
            VerificationPolicy.stateOf(now.minus(Duration.ofHours(48)).minusMillis(1), now),
        )
    }

    @Test
    fun `check exactly at the stale window still reads stale`() {
        assertEquals(
            VerificationState.STALE,
            VerificationPolicy.stateOf(now.minus(Duration.ofDays(7)), now),
        )
    }

    @Test
    fun `check past the stale window reads expired`() {
        assertEquals(
            VerificationState.EXPIRED,
            VerificationPolicy.stateOf(now.minus(Duration.ofDays(7)).minusMillis(1), now),
        )
    }

    @Test
    fun `future-dated check reads verified`() {
        // Clock skew between the pipeline and the client must not shout.
        assertEquals(
            VerificationState.VERIFIED,
            VerificationPolicy.stateOf(now.plus(Duration.ofHours(1)), now),
        )
    }

    @Test
    fun `a report outranks every freshness reading`() {
        assertEquals(
            VerificationState.REPORTED,
            VerificationPolicy.stateOf(verified, now, reportedAt = verified),
        )
    }
}
