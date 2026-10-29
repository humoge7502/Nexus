package com.nexus.explore

import androidx.compose.ui.res.stringResource
import com.nexus.explore.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.UiState
import java.time.LocalDate

@Composable
fun HackathonDetailScreen(
    hackathonId: String,
    onBack: () -> Unit = {},
    viewModel: HackathonDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.hackathons.collectAsStateWithLifecycle()
    val hackathon = (state as? UiState.Success<List<Hackathon>>)?.data?.find { it.id == hackathonId }
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.explore_hackathon),
            onBack = onBack,
            actions = {
                IconButton(onClick = { hackathon?.let { viewModel.toggleSave(it.id) } }) {
                    Icon(
                        if (hackathon?.isSaved == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (hackathon?.isSaved == true) {
                            stringResource(R.string.cd_remove_from_saved)
                        } else {
                            stringResource(R.string.cd_save)
                        },
                        tint = if (hackathon?.isSaved == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            },
        )

        if (hackathon == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (state) {
                    is UiState.Loading -> CircularProgressIndicator()
                    is UiState.Error -> Text(
                        (state as UiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp),
                    )
                    else -> Text(stringResource(R.string.explore_hackathon_not_found), style = MaterialTheme.typography.bodyLarge)
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NexusCard(
                gradient = NexusGradients.PrimaryDeep,
                shape = NexusShapes.lg,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(hackathon.title, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text(hackathon.organizer, style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f))
                    if (hackathon.isLive) {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(8.dp))
                            .background(NexusColors.Rose400.copy(alpha = 0.22f))
                            .padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Text(stringResource(R.string.explore_live), fontWeight = FontWeight.Bold,
                                color = NexusColors.Rose400)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DetailStatCard(
                    icon = Icons.Default.CalendarMonth,
                    label = stringResource(R.string.explore_start),
                    value = hackathon.startDate.toString(),
                    modifier = Modifier.weight(1f),
                )
                DetailStatCard(
                    icon = Icons.Default.Groups,
                    label = stringResource(R.string.explore_team_size),
                    value = "${hackathon.teamSize.first}-${hackathon.teamSize.last}",
                    modifier = Modifier.weight(1f),
                )
                DetailStatCard(
                    icon = Icons.Default.Language,
                    label = stringResource(R.string.explore_mode),
                    value = hackathon.mode.name,
                    modifier = Modifier.weight(1f),
                )
            }

            if (hackathon.description.isNotBlank()) {
                NexusCard(shape = NexusShapes.md) {
                    Column {
                        Text(stringResource(R.string.explore_description), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(hackathon.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            NexusCard(shape = NexusShapes.md) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(stringResource(R.string.explore_prize_pool), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(hackathon.prizePool, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = NexusColors.Amber400)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.explore_deadline), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(hackathon.registrationDeadline.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (hackathon.registrationDeadline.isBefore(LocalDate.now()))
                                NexusColors.Rose400 else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (hackathon.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (tag in hackathon.tags) {
                        Box(Modifier.clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(tag, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (hackathon.registrationUrl.isNotBlank()) {
                        uriHandler.openUri(hackathon.registrationUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.explore_register_now), fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    NexusCard(
        modifier = modifier,
        shape = NexusShapes.sm,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
