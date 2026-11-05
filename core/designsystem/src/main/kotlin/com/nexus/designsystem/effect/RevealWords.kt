package com.nexus.designsystem.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion

/**
 * The serif statement arriving the way it is read — word by word.
 *
 * React Bits' SplitText, cut for the byline voice: words rise a few points
 * and fade in, ordered by reading order, off ONE clock so the line arrives
 * as a single gesture rather than a cascade of independent timers. The
 * stagger stays proportional to length — a two-word greeting resolves in
 * ~200ms, inside the screen-entrance budget — and TalkBack hears one text
 * node, not a scattering of words.
 *
 * Reduced motion renders a plain [Text]; the words are already there.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevealWords(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val reducedMotion = LocalReducedMotion.current
    val words = remember(text) { text.split(' ').filter(String::isNotBlank) }
    if (reducedMotion || words.isEmpty()) {
        Text(text = text, modifier = modifier, style = style, color = color)
        return
    }

    val wordDelayMs = 45
    val wordDurationMs = 160
    val totalMs = (words.size - 1) * wordDelayMs + wordDurationMs
    val clock = remember { Animatable(0f) }
    LaunchedEffect(text) {
        clock.snapTo(0f)
        clock.animateTo(1f, tween(durationMillis = totalMs, easing = LinearEasing))
    }
    val liftPx = with(LocalDensity.current) { 6.dp.toPx() }

    FlowRow(
        modifier = modifier.clearAndSetSemantics { contentDescription = text },
    ) {
        words.forEachIndexed { index, word ->
            val windowStartMs = index * wordDelayMs
            val progress = ((clock.value * totalMs - windowStartMs) / wordDurationMs)
                .coerceIn(0f, 1f)
            Text(
                text = if (index == words.lastIndex) word else "$word ",
                style = style,
                color = color,
                modifier = Modifier.graphicsLayer {
                    alpha = progress
                    translationY = (1f - progress) * liftPx
                },
            )
        }
    }
}
