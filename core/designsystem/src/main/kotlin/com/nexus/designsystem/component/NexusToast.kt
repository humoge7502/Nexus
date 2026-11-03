package com.nexus.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import kotlinx.coroutines.delay

/**
 * How a transient message reads, not what it says: the tone selects the colour
 * of the toast's leading bar and nothing else. The plate itself stays
 * ink-on-surface in every state, because a toast is a reading on the
 * instrument — not a repaint of it — and the one-signal-per-screen law is not
 * suspended for feedback.
 */
enum class ToastTone {
    /** An ordinary confirmation; the structural hairline grey. */
    Neutral,

    /** A milestone or success worth marking in signal. */
    Signal,

    /** A boundary the user just hit — error-adjacent, never decorative. */
    Attention,

    /** Something failed and the user should know it. */
    Critical,
}

/**
 * One transient message: what it says, how it reads, and an identity a host
 * can use to tell a repeated message apart from the one leaving the screen.
 */
data class NexusToastData(
    val message: String,
    val tone: ToastTone = ToastTone.Neutral,
    val id: Long = 0,
)

/**
 * Holds the single toast currently on screen, with SnackbarHostState's show
 * ergonomics so a screen's `effect.collect { show(...) }` loop migrates
 * without rethinking itself.
 *
 * [show] suspends for the display duration and clears the plate before it
 * returns, which keeps serial effect loops serial exactly as showSnackbar
 * did. A generation counter guards the clear: a newer show replaces the plate
 * on screen immediately, and the superseded timer declines to blank out its
 * replacement when it eventually wakes up.
 */
class NexusToastState {

    // Private and write-through-show() on purpose: the generation invariant
    // below only holds if nothing else can clear or overwrite the plate.
    private var plate by mutableStateOf<NexusToastData?>(null)

    /** The toast currently on screen, or null while the plate is empty. */
    val current: NexusToastData?
        get() = plate

    private var generation = 0

    /**
     * Shows [data] for the display duration, then clears it. Suspending, so
     * the caller waits out the toast exactly as it waited out a snackbar.
     */
    suspend fun show(data: NexusToastData) {
        val slot = ++generation
        plate = data
        try {
            delay(DISPLAY_MILLIS)
        } finally {
            // Only the generation that still owns the slot may clear it; a
            // newer show() has already taken the plate over.
            if (slot == generation) plate = null
        }
    }
}

/** Remembers a [NexusToastState] for the lifetime of the composition. */
@Composable
fun rememberNexusToastState(): NexusToastState = remember { NexusToastState() }

/**
 * The screen-level host for toasts.
 *
 * Place it wherever the screen wants transient feedback — the Scaffold
 * snackbar slot or a Box overlay — and it bottom-centers the current plate,
 * arriving with a short fade-and-rise. Auto-dismiss never blocks input: a
 * toast is an overlay the user reads, not a modal they answer.
 */
@Composable
fun NexusToastHost(
    state: NexusToastState,
    modifier: Modifier = Modifier,
) {
    val toast = state.current
    // Keep the last non-null toast so the exit animation has content to
    // animate: state.current is already null while the plate is still leaving.
    // The write is safe in composition because the guard reads only
    // state.current, never the value being written.
    var displayed by remember { mutableStateOf<NexusToastData?>(null) }
    if (toast != null) {
        displayed = toast
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(animationSpec = toastSpec(entering = true)) +
                slideInVertically(
                    animationSpec = toastSpec(entering = true),
                    initialOffsetY = { it / 3 },
                ),
            exit = fadeOut(animationSpec = toastSpec(entering = false)) +
                slideOutVertically(
                    animationSpec = toastSpec(entering = false),
                    targetOffsetY = { it / 3 },
                ),
        ) {
            displayed?.let { data ->
                // The plate owns its gutter so no call site repeats it.
                NexusToastPlate(data = data, modifier = Modifier.padding(NexusSpacing.md))
            }
        }
    }
}

/**
 * Entrance resolves at the standard clock and exits faster, both under the
 * 300ms rule; both collapse to an instant change through [motionMillis] when
 * the user has asked for reduced motion.
 */
@Composable
private fun <T> toastSpec(entering: Boolean) = tween<T>(
    durationMillis = motionMillis(
        if (entering) NexusAnimation.Duration.standard else NexusAnimation.Duration.fast,
    ),
    easing = if (entering) {
        NexusAnimation.Easing.emphasizedDecelerate
    } else {
        NexusAnimation.Easing.emphasizedAccelerate
    },
)

/**
 * The toast plate: an ink-on-surface reading with a tone-coloured leading bar,
 * a hairline border and a subtle tonal lift. It is floating chrome, so the
 * tonal elevation is the sanctioned kind — the same allowance the bottom bar
 * gets.
 */
@Composable
private fun NexusToastPlate(
    data: NexusToastData,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.nexus
    val toneColor = when (data.tone) {
        ToastTone.Neutral -> ext.hairlineStrong
        ToastTone.Signal -> ext.signal
        ToastTone.Attention -> ext.attention
        ToastTone.Critical -> MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
        },
        shape = NexusShapes.md,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp,
        border = BorderStroke(NexusSpacing.hairline, ext.hairlineStrong),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(NexusShapes.pill)
                    .background(toneColor),
            )
            Text(text = data.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** A toast is read, not studied: two and a half seconds, then it leaves. */
private const val DISPLAY_MILLIS = 2_500L
