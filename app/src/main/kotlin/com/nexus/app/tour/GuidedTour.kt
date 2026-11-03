package com.nexus.app.tour

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.app.R
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusNavItems
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.nexus

/**
 * One stop per real destination. `station` is the index into NexusNavItems, so
 * the tour and the chrome cannot drift apart, and a stop with station = -1 has
 * no place to point at — it is the closing card.
 */
internal data class TourStop(
    val station: Int,
    val title: Int,
    val body: Int,
)

internal val TourStops = listOf(
    TourStop(0, R.string.tour_home_title, R.string.tour_home_body),
    TourStop(1, R.string.tour_explore_title, R.string.tour_explore_body),
    TourStop(2, R.string.tour_ai_title, R.string.tour_ai_body),
    TourStop(3, R.string.tour_growth_title, R.string.tour_growth_body),
    TourStop(4, R.string.tour_profile_title, R.string.tour_profile_body),
    TourStop(-1, R.string.tour_close_title, R.string.tour_close_body),
)

/**
 * The first-run walkthrough. Each step navigates to the section it describes and
 * cuts a hole in the scrim around that section's own station in the bar, so a
 * student is shown where the thing is rather than told about it in the abstract.
 */
@Composable
internal fun GuidedTour(
    stationsBounds: Rect?,
    onNavigateToStation: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val current = TourStops[step]
    val isLast = step == TourStops.lastIndex
    val density = LocalDensity.current
    val extended = MaterialTheme.nexus

    LaunchedEffect(step) {
        if (current.station >= 0) onNavigateToStation(current.station)
    }

    // The tour walks the bar and ends on Profile; dropping back to Home is what
    // makes finishing feel like arriving rather than being left somewhere.
    val endTour: () -> Unit = {
        onNavigateToStation(0)
        onFinish()
    }

    val hole = rememberTourHole(stationsBounds, current.station)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightPx = with(density) { maxHeight.toPx() }

        TourScrim(hole = hole, signal = extended.signal)

        val cardBottomInset = if (hole.highlighted) (screenHeightPx - hole.top).coerceAtLeast(0f) else 0f

        Surface(
            modifier = Modifier
                .align(if (hole.highlighted) Alignment.BottomCenter else Alignment.Center)
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = with(density) { (cardBottomInset + 18.dp.toPx()).toDp() },
                )
                .semantics { liveRegion = LiveRegionMode.Polite },
            shape = NexusShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = stringResource(R.string.tour_step_of, step + 1, TourStops.size),
                    fontSize = 11.sp,
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extended.signal,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(current.title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(current.body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TourPips(step = step, modifier = Modifier.weight(1f))
                    TextButton(onClick = endTour) {
                        Text(
                            text = stringResource(R.string.tour_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    NexusButton(
                        text = stringResource(if (isLast) R.string.tour_start else R.string.tour_next),
                        onClick = { if (isLast) endTour() else step++ },
                        gradient = listOf(extended.signal),
                        shape = NexusShapes.sm,
                        expand = false,
                    )
                }
            }
        }
    }
}

/**
 * The rectangle the tour points at, in canvas pixels, mid-animation. A closing
 * card has no station to highlight, so it carries no hole and fades out.
 */
internal class TourHole(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alpha: Float,
    val highlighted: Boolean,
) {
    val rect: Rect get() = Rect(Offset(left, top), Offset(right, bottom))

    /** Anything thinner than a pixel is a rounding artefact, not a spotlight. */
    val visible: Boolean get() = alpha > 0.01f && right - left > 1f && bottom - top > 1f
}

/**
 * The hole travels between stations rather than teleporting, which is what makes
 * the bar read as one machined part being pointed at.
 */
@Composable
private fun rememberTourHole(stationsBounds: Rect?, station: Int): TourHole {
    val reducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val insetX = with(density) { 3.dp.toPx() }
    val insetY = with(density) { 4.dp.toPx() }
    val cellWidth = (stationsBounds?.width ?: 0f) / NexusNavItems.size
    val target: Rect? = stationsBounds
        ?.takeIf { station >= 0 && it.width > 0f }
        ?.let { bar ->
            val left = bar.left + cellWidth * station
            Rect(
                Offset(left + insetX, bar.top + insetY),
                Offset(left + cellWidth - insetX, bar.bottom - insetY),
            )
        }
    val spec = if (reducedMotion) {
        snap<Float>()
    } else {
        spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
    }
    val left by animateFloatAsState(target?.left ?: 0f, spec, label = "holeLeft")
    val top by animateFloatAsState(target?.top ?: 0f, spec, label = "holeTop")
    val right by animateFloatAsState(target?.right ?: 0f, spec, label = "holeRight")
    val bottom by animateFloatAsState(target?.bottom ?: 0f, spec, label = "holeBottom")
    val alpha by animateFloatAsState(if (target == null) 0f else 1f, spec, label = "holeAlpha")
    return TourHole(
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        alpha = alpha,
        highlighted = target != null,
    )
}

/**
 * The dimming scrim with the spotlight cut out of it. Drawn with an even-odd
 * path: the hole must not punch through the screen behind the overlay, and that
 * needs no offscreen compositing layer to be correct.
 */
@Composable
private fun TourScrim(hole: TourHole, signal: Color) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { 14.dp.toPx() }
    val strokePx = with(density) { 2.dp.toPx() }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { /* the tour drives navigation; background taps are deliberately ignored */ }
    ) {
        val holeRect = hole.rect
        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            if (hole.visible) addRoundRect(RoundRect(holeRect, CornerRadius(cornerRadiusPx)))
        }
        drawPath(path, Color.Black.copy(alpha = 0.76f))
        if (hole.visible) {
            drawRoundRect(
                color = signal.copy(alpha = 0.9f * hole.alpha),
                topLeft = holeRect.topLeft,
                size = holeRect.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                style = Stroke(width = strokePx),
            )
        }
    }
}

@Composable
private fun TourPips(step: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TourStops.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .size(if (index == step) 9.dp else 6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (index == step) 1f else 0.32f,
                        ),
                        shape = CircleShape,
                    ),
            )
            if (index != TourStops.lastIndex) Spacer(Modifier.width(5.dp))
        }
    }
}
