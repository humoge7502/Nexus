package com.nexus.settings

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexus.designsystem.component.NexusIconButton

/**
 * The walkthrough film. The asset is footage captured from the running app on a
 * device, so the screens a student watches here are the screens they get — not a
 * studio render of a design that was never built.
 */
@Composable
fun WalkthroughFilmDialog(onDismiss: () -> Unit) {
    var player by remember { mutableStateOf<VideoView?>(null) }
    var prepared by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { player?.stopPlayback() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.walkthrough_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(R.string.walkthrough_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
                NexusIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = stringResource(R.string.walkthrough_close),
                    onClick = onDismiss,
                    tint = Color.White,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(role = Role.Button) {
                        if (finished) {
                            player?.seekTo(0)
                            player?.start()
                            finished = false
                            playing = true
                        } else {
                            playing = !playing
                            if (playing) player?.start() else player?.pause()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setOnPreparedListener {
                                it.isLooping = false
                                prepared = true
                                start()
                            }
                            setOnCompletionListener {
                                playing = false
                                finished = true
                            }
                            setVideoURI(
                                Uri.parse("android.resource://${ctx.packageName}/${R.raw.sagitta_walk}"),
                            )
                        }.also { player = it }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                if (!prepared) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                }
                if (!playing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                if (finished) R.string.walkthrough_replay else R.string.walkthrough_play,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }

            val stations = stringResource(R.string.walkthrough_stations)
            Text(
                text = stations,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .semantics { contentDescription = stations },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
