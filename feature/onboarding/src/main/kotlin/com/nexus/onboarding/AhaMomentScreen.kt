package com.nexus.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusShapes

/**
 * The one full-lime slab in the product: a completion certificate, not a screen.
 *
 * Lime is a light signal, so every mark on it is ink. The medallion and XP panel
 * are ink tints rather than white glass, which keeps the surface reading as one
 * printed card instead of a stack of translucent chips.
 */
@Composable
fun AhaMomentScreen(
    displayName: String,
    xpAwarded: Int,
    onContinue: () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val scale = remember { Animatable(if (reducedMotion) 1f else 0.9f) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            scale.snapTo(1f)
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 520,
                    easing = NexusAnimation.Easing.emphasizedDecelerate,
                ),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NexusColors.SignalBright, NexusColors.Signal),
                ),
            )
            .instrumentGrid(spacingDp = 28f, alpha = 0.05f, color = NexusColors.SignalInk),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(NexusColors.SignalInk.copy(alpha = 0.07f))
                    .border(1.dp, NexusColors.SignalInk.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NexusColors.SignalInk,
                    modifier = Modifier.size(56.dp),
                )
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = stringResource(R.string.text_welcome_to_nexus),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = NexusColors.SignalInk,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.aha_dashboard_ready, displayName),
                style = MaterialTheme.typography.bodyLarge,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .clip(NexusShapes.md)
                    .background(NexusColors.SignalInk.copy(alpha = 0.08f))
                    .border(1.dp, NexusColors.SignalInk.copy(alpha = 0.16f), NexusShapes.md)
                    .padding(horizontal = 28.dp, vertical = 22.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.aha_xp_awarded, xpAwarded),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.SignalInk,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.text_awarded_for_completing_your_profile),
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.SignalInk.copy(alpha = 0.86f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.text_start_tracking_internships_earning),
                style = MaterialTheme.typography.bodyMedium,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = NexusShapes.md,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexusColors.SignalInk,
                    contentColor = NexusColors.SignalBright,
                ),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_start_exploring),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
