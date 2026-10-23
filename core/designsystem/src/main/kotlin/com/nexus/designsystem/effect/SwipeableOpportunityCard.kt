package com.nexus.designsystem.effect

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.animation.NexusSpring
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SwipeableOpportunityCard(
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 300f
    val cardAlpha by animateFloatAsState(
        targetValue = if (abs(offsetX) > dismissThreshold / 2) 0.7f else 1f,
        animationSpec = NexusSpring.Medium,
    )
    val rotation by animateFloatAsState(
        targetValue = (offsetX / 50f).coerceIn(-15f, 15f),
        animationSpec = NexusSpring.Light,
    )
    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .rotate(rotation)
            .alpha(cardAlpha)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > dismissThreshold -> onSave()
                            offsetX < -dismissThreshold -> onDismiss()
                            else -> { offsetX = 0f; offsetY = 0f }
                        }
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y * 0.3f
                    },
                )
            },
        content = { content() },
    )
}
