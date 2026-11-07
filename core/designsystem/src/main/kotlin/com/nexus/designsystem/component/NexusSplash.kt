package com.nexus.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/**
 * Branded boot surface. The system splash (themes.xml) shows the N mark on
 * ink for ~800ms; this Compose layer carries the handoff to auth/main so
 * the "loading" step is never a blank window.
 *
 * One press of restraint by design: the mark, the tracked wordmark, the
 * serif promise ("Placement, measured."), and a single signal hairline.
 * No spinner circus, no gradient wash, no infinite barber-pole. The
 * indeterminate indicator collapses to a static lit segment under
 * [LocalReducedMotion] — the wait is still legible, just still.
 */
@Composable
fun NexusSplash(
    modifier: Modifier = Modifier,
    statusText: String = "Loading your workspace",
    tagline: String = "Placement, measured.",
) {
    val ext = MaterialTheme.nexus
    val reducedMotion = LocalReducedMotion.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics(mergeDescendants = true) { contentDescription = "Loading Nexus" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(NexusSpacing.lg),
        ) {
            NexusMark(size = 96.dp)
            Spacer(Modifier.height(NexusSpacing.md))
            Text(
                text = "NEXUS",
                style = NexusTypography.DataLabel.copy(fontSize = 14.sp, letterSpacing = 4.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = tagline,
                style = NexusTypography.Voice.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(NexusSpacing.xxl))
            if (reducedMotion) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(2.dp)
                        .clip(NexusShapes.xs)
                        .background(ext.gaugeTrack),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.42f)
                            .height(2.dp)
                            .clip(NexusShapes.xs)
                            .background(ext.signal),
                    )
                }
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(160.dp)
                        .height(2.dp)
                        .clip(NexusShapes.xs),
                    color = ext.signal,
                    trackColor = ext.gaugeTrack,
                    strokeCap = StrokeCap.Round,
                )
            }
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = statusText,
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
