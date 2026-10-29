package com.nexus.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
@Suppress("ComposableLambdaParameterNaming")
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    lottieResId: Int? = null,
    icon: @Composable (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            lottieResId != null -> {
                val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
                val progress = animateLottieCompositionAsState(
                    composition = composition.value,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = composition.value,
                    progress = { progress.value },
                    modifier = Modifier.size(200.dp)
                )
            }
            icon != null -> icon()
            else -> EmptyMark()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun EmptyMark() {
    val track = MaterialTheme.colorScheme.outlineVariant
    val signal = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width),
                style = stroke,
            )
            drawArc(
                color = signal,
                startAngle = -90f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width),
                style = stroke,
            )
            drawCircle(color = signal, radius = 3.dp.toPx())
        }
    }
}
