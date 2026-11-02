package com.nexus.designsystem.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * The reveal layer: how content arrives, in one vocabulary.
 *
 * [NexusAnimation] fixes duration and easing for a control answering a finger.
 * This file answers the other question — what happens when a screen fills with
 * material that was not there a moment ago. Two rules govern it.
 *
 * Order carries meaning. A stagger exists to say "read top-down", so it belongs
 * only to a sequence the user is meant to traverse in order: a hero, then its
 * readings, then what changed. Rows of equal rank arrive together, because a
 * stagger between peers implies a ranking that isn't real.
 *
 * One clock per reveal. Rather than five animations starting in five stray
 * frames, [revealClock] runs a single 0→1 timeline and each element owns a window
 * of it. That is what makes a signature interaction read as one gesture instead of
 * a pile of coincidences — and it is why a gauge sweep and the number inside it can
 * never disagree: they are the same value, drawn twice.
 *
 * Under [LocalReducedMotion] the clock is already at 1 on its first frame, so every
 * reveal collapses to its finished state with no animation running at all.
 */

/** A peer waits this slice of its own window per rank, so the stagger reads the
 * same whether the reveal runs 400ms or a second. */
private const val PeerStagger = 0.15f

object NexusReveal {
    /** Sequence windows inside a single reveal, as fractions of the shared clock. */
    object Window {
        /** The primary instrument. */
        val hero = 0f to 0.52f
        /** The statement beside it. */
        val headline = 0.18f to 0.68f
        /** The readings beneath, staggered among themselves. */
        val readings = 0.42f to 0.94f
        /** The last thing to land: what moved since the last reading. */
        val footnote = 0.72f to 1f
    }

    /** The easing a reveal uses: hard in, long settle, no overshoot. */
    val Emphasis = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
}

/**
 * A shared 0→1 clock for one reveal, linear by design.
 *
 * Linear is correct because this is a *timeline*, not a movement: each element
 * applies [NexusReveal.Emphasis] inside its own window. Easing the master clock
 * would cram every window into the first fraction of the reveal and leave the tail
 * dead.
 */
@Composable
fun revealClock(durationMillis: Int = NexusAnimation.Duration.dramatic): Float {
    if (LocalReducedMotion.current) return 1f
    val clock = remember { Animatable(0f) }
    LaunchedEffect(clock, durationMillis) {
        clock.animateTo(1f, tween(durationMillis, easing = LinearEasing))
    }
    return clock.value
}

/**
 * Where an element sits in its window: 0 before it starts, 1 once settled.
 * [rank] staggers peers that share a window; each peer still finishes exactly as
 * the clock ends, so a long list never trails past the reveal.
 */
fun revealFraction(
    clock: Float,
    window: Pair<Float, Float>,
    rank: Int = 0,
    ease: CubicBezierEasing = NexusReveal.Emphasis,
): Float {
    val span = (window.second - window.first).coerceAtLeast(0.001f)
    val start = window.first + rank * PeerStagger * span
    val remaining = (window.second - start).coerceAtLeast(0.001f)
    val local = ((clock - start) / remaining).coerceIn(0f, 1f)
    return ease.transform(local)
}

/** True once the clock is past [window] — the gate for a value that must never be
 * seen counting, like a score a user may act on. */
fun revealSettled(clock: Float, window: Pair<Float, Float>): Boolean = clock >= window.second

/**
 * Page motion for the shell.
 *
 * Changing tabs fades and lifts a few percent: destinations are peers, and
 * sliding a peer sideways would claim an order the navigation does not have.
 * Opening a detail does slide, because there the parent genuinely sits behind the
 * child.
 */
object NexusTransitions {
    /** Peer-to-peer: two destinations on the same level. */
    fun peerEnter(index: Int = 0): EnterTransition =
        fadeIn(
            animationSpec = tween(
                durationMillis = NexusAnimation.Duration.standard,
                delayMillis = index * NexusAnimation.Duration.fast,
                easing = NexusAnimation.Easing.emphasizedDecelerate,
            ),
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = NexusAnimation.Duration.emphasized,
                delayMillis = index * NexusAnimation.Duration.fast,
                easing = NexusAnimation.Easing.emphasizedDecelerate,
            ),
            initialOffsetY = { (it * 0.12f).toInt() },
        )

    fun peerExit(): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.fast,
            easing = NexusAnimation.Easing.emphasizedAccelerate,
        ),
    )

    /** Parent-to-child: a detail in front of the list it opened from. */
    fun forwardEnter(): EnterTransition = slideInHorizontally(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.emphasized,
            easing = NexusAnimation.Easing.emphasizedDecelerate,
        ),
        initialOffsetX = { (it * 0.28f).toInt() },
    ) + fadeIn(animationSpec = tween(NexusAnimation.Duration.fast))

    fun forwardExit(): ExitTransition = slideOutHorizontally(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.standard,
            easing = NexusAnimation.Easing.emphasizedAccelerate,
        ),
        targetOffsetX = { -(it * 0.16f).toInt() },
    ) + fadeOut(animationSpec = tween(NexusAnimation.Duration.fast))

    /** Child-to-parent: returning to the list a detail came from. */
    fun backwardEnter(): EnterTransition = slideInHorizontally(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.emphasized,
            easing = NexusAnimation.Easing.emphasizedDecelerate,
        ),
        initialOffsetX = { -(it * 0.16f).toInt() },
    ) + fadeIn(animationSpec = tween(NexusAnimation.Duration.fast))

    fun backwardExit(): ExitTransition = slideOutHorizontally(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.standard,
            easing = NexusAnimation.Easing.emphasizedAccelerate,
        ),
        targetOffsetX = { (it * 0.16f).toInt() },
    ) + fadeOut(animationSpec = tween(NexusAnimation.Duration.fast))

    /** A plate arriving inside a screen already open — a sheet, an expanded panel. */
    fun plateEnter(index: Int = 0): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.emphasized,
            delayMillis = index * NexusAnimation.Duration.fast,
            easing = NexusAnimation.Easing.enter,
        ),
    ) + slideInVertically(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.long,
            delayMillis = index * NexusAnimation.Duration.fast,
            easing = NexusAnimation.Easing.enter,
        ),
        initialOffsetY = { (it * 0.5f).toInt() },
    )

    fun plateExit(): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.fast,
            easing = NexusAnimation.Easing.exit,
        ),
    ) + slideOutVertically(
        animationSpec = tween(
            durationMillis = NexusAnimation.Duration.standard,
            easing = NexusAnimation.Easing.exit,
        ),
        targetOffsetY = { (it * 0.4f).toInt() },
    )
}

/**
 * The single value an arc and its number both read.
 *
 * Living here rather than in each chart is the point: a gauge sweep and the count
 * inside it are one animation observed twice, not two animations hoping to land
 * together.
 */
@Composable
fun sweepValue(
    target: Float,
    settled: Boolean,
    durationMillis: Int = NexusAnimation.Duration.long,
): Float {
    val value by animateFloatAsState(
        targetValue = if (settled) target else 0f,
        animationSpec = tween(
            durationMillis = motionMillis(durationMillis),
            easing = NexusAnimation.Easing.emphasizedDecelerate,
        ),
        label = "sweep",
    )
    return value
}
