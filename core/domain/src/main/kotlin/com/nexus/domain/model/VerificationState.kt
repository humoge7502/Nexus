package com.nexus.domain.model

import java.time.Duration
import java.time.Instant

/**
 * The lifecycle of a provenance check on an opportunity. Provenance is the
 * product's trust moat, so freshness is a domain concept rather than
 * per-screen threshold arithmetic: every renderer speaks the same vocabulary,
 * and the windows live here where tests pin them, instead of duplicated as
 * constants that drift apart between screens.
 */
enum class VerificationState {
    /** The pipeline has never confirmed this listing's apply link. */
    UNVERIFIED,

    /** The link was confirmed recently — fresh renders as silence. */
    VERIFIED,

    /** Confirmed within living memory but aging; speak neutrally. */
    STALE,

    /** Confirmed so long ago the deadline may have moved; speak as a fault. */
    EXPIRED,

    /**
     * A reader reported the listing. Reserved for the user-report flow — no
     * producer writes it yet; the state exists so renderers already speak it
     * when that flow lands.
     */
    REPORTED,
}

/**
 * Single source of truth for verification freshness. The thresholds mirror the
 * bands that shipped first (48 h quiet window, 7 day stale window) so adopting
 * the policy is behaviour-preserving; screens read states instead of
 * re-deriving ages against private constants.
 */
object VerificationPolicy {

    /** Inside this window the check is fresh and renderers say nothing. */
    val QUIET_WINDOW: Duration = Duration.ofHours(48)

    /** Beyond this window the check has expired, not merely gone stale. */
    val STALE_WINDOW: Duration = Duration.ofDays(7)

    /**
     * Maps a provenance check to its state. Boundaries are strict — a check
     * exactly [QUIET_WINDOW] old still reads VERIFIED and one exactly
     * [STALE_WINDOW] old still reads STALE — matching the shipped bands, so
     * the rendered silence/neutral/fault cutover moments do not move.
     */
    fun stateOf(
        verifiedAt: Instant?,
        now: Instant,
        reportedAt: Instant? = null,
    ): VerificationState = when {
        reportedAt != null -> VerificationState.REPORTED
        verifiedAt == null -> VerificationState.UNVERIFIED
        Duration.between(verifiedAt, now) > STALE_WINDOW -> VerificationState.EXPIRED
        Duration.between(verifiedAt, now) > QUIET_WINDOW -> VerificationState.STALE
        else -> VerificationState.VERIFIED
    }
}
