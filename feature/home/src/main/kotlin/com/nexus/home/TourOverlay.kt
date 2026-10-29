package com.nexus.home

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexus.home.R

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.R as DesignSystemR
import com.nexus.designsystem.theme.NexusGradients

private data class TourStep(
    val title: String,
    val body: String,
)

private val tourSteps = listOf(
    TourStep(
        "Welcome to Ascent",
        "This is your mission control. Everything here exists to move your Career Readiness score up.",
    ),
    TourStep(
        "Explore opportunities",
        "Tap Explore in the bottom bar for internships, hackathons and certifications — updated for you.",
    ),
    TourStep(
        "Grow your score",
        "The Growth tab holds your Ascent Score, skill radar and DSA tracker. Solve one problem today to start your streak.",
    ),
    TourStep(
        "AI that knows you",
        "The AI tab is your co-pilot: chat, Deep Research with cited answers, resume analysis and roadmaps.",
    ),
)

/**
 * First-launch guided tour. A dimmed full-screen overlay with a progress
 * card; four steps that map to the app's real navigation, so a new student
 * knows where everything is before the first tap.
 */
@Composable
fun TourOverlay(onFinish: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var showDemo by remember { mutableStateOf(false) }
    val current = tourSteps[step]
    val isLast = step == tourSteps.lastIndex

    if (showDemo) {
        DemoVideoDialog(onDismiss = { showDemo = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(NexusGradients.Primary),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.home_step_of, step + 1, tourSteps.size),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                current.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                current.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tourSteps.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 10.dp else 6.dp)
                            .background(
                                Color.White.copy(alpha = if (i == step) 1f else 0.4f),
                                CircleShape,
                            ),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (isLast) onFinish() else step++ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    if (isLast) "Let's go" else "Next",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            TextButton(onClick = { showDemo = true }) {
                Text(
                    "▶ Watch the 20-second demo",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (!isLast) {
                TextButton(onClick = onFinish) {
                    Text(stringResource(R.string.home_skip_tour), color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

/**
 * Plays the bundled launch film (real screens of every section, with the
 * signature gradient branding) so a first-run student sees the whole app
 * before tapping through the steps. Looping; dismiss to return to the tour.
 */
@Composable
private fun DemoVideoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val controller = MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setVideoURI(
                            Uri.parse(
                                "android.resource://${ctx.packageName}/${DesignSystemR.raw.ascent_demo}",
                            ),
                        )
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mp.start()
                        }
                    }
                },
                onRelease = { it.stopPlayback() },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.62f),
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.BottomCenter).padding(28.dp),
            ) {
                Text("Back to tour", color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}
