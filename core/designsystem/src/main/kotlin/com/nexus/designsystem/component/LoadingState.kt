package com.nexus.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.R
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.nexus

@Composable
fun Modifier.shimmer(): Modifier = composed {
    val ext = MaterialTheme.nexus
    // The sweep is an infinite animation; with reduced motion it renders as a
    // static placeholder tone instead of looping forever. The base is the
    // theme-aware gauge track (ink @ ~17% on pearl, bone @ ~10% on ink) so
    // the placeholder is legible in both themes — onSurface @ 7% disappears
    // on paper and the list looks unpainted.
    if (com.nexus.designsystem.theme.LocalReducedMotion.current) {
        return@composed this.background(ext.gaugeTrack)
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX",
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            ),
            start = Offset(translateX, 0f),
            end = Offset(translateX + 400f, 0f),
        )
    )
}

/**
 * A page-level placeholder.
 *
 * [loadingLabel] defaults to "Loading" so that every call site is announced by
 * TalkBack without the caller having to remember; pass a module-specific phrase
 * when the wait has a name ("Loading your streak").
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    loadingLabel: String? = stringResource(R.string.ds_loading),
) {
    ShimmerCard(modifier = modifier, loadingLabel = loadingLabel)
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    loadingLabel: String? = stringResource(R.string.ds_loading),
) {
    Column(
        modifier = modifier.padding(NexusSpacing.screenEdge).semantics(mergeDescendants = true) {
            if (loadingLabel != null) contentDescription = loadingLabel
        },
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(24.dp)
                .clip(NexusShapes.sm)
                .shimmer()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(NexusShapes.sm)
                .shimmer()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(16.dp)
                .clip(NexusShapes.sm)
                .shimmer()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
