package com.nexus.explore

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.domain.model.EventMode
import com.nexus.domain.model.Hackathon
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

private val hackathonSamples = listOf(
    Hackathon("h1", "Smart India Hackathon", "SIH", "National-level hackathon", EventMode.HYBRID, "Delhi",
        LocalDate.now(), LocalDate.now().plusDays(2), LocalDate.now().plusDays(3),
        "₹1,00,000", registrationUrl = "https://sih.gov.in", isLive = true,
        tags = persistentListOf("AI", "Web")),
    Hackathon("h2", "MLH Hackathon", "Major League Hacking", "Build something amazing", EventMode.ONLINE, null,
        LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), LocalDate.now().plusDays(1),
        "$5,000", registrationUrl = "https://mlh.io", isLive = true,
        tags = persistentListOf("All")),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonDetailScreen(
    hackathonId: String,
    onBack: () -> Unit = {},
) {
    val hackathon = remember(hackathonId) { hackathonSamples.find { it.id == hackathonId } }
    var isSaved by remember { mutableStateOf(hackathon?.isSaved ?: false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Hackathon", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { isSaved = !isSaved }) {
                    Icon(
                        if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        if (hackathon == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hackathon not found", style = MaterialTheme.typography.bodyLarge)
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
                gradient = NexusGradients.Stellar,
                shape = NexusShapes.lg,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "\uD83C\uDFC6", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(hackathon.title, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text(hackathon.organizer, style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f))
                    if (hackathon.isLive) {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Text("LIVE", fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444))
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
                    label = "Start",
                    value = hackathon.startDate.toString(),
                    modifier = Modifier.weight(1f),
                )
                DetailStatCard(
                    icon = Icons.Default.Groups,
                    label = "Team Size",
                    value = "${hackathon.teamSize.first}-${hackathon.teamSize.last}",
                    modifier = Modifier.weight(1f),
                )
                DetailStatCard(
                    icon = Icons.Default.Language,
                    label = "Mode",
                    value = hackathon.mode.name,
                    modifier = Modifier.weight(1f),
                )
            }

            if (hackathon.description.isNotBlank()) {
                NexusCard(shape = NexusShapes.md) {
                    Text("Description", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(hackathon.description, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            NexusCard(shape = NexusShapes.md) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Prize Pool", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(hackathon.prizePool, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = NexusColors.StellarPink)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Deadline", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(hackathon.registrationDeadline.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (hackathon.registrationDeadline.isBefore(LocalDate.now()))
                                Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (hackathon.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (tag in hackathon.tags) {
                        Box(Modifier.clip(RoundedCornerShape(8.dp))
                            .background(NexusColors.Indigo500.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(tag, style = MaterialTheme.typography.labelMedium,
                                color = NexusColors.Indigo400)
                        }
                    }
                }
            }

            Button(
                onClick = { /* open URL */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexusColors.NexusBlue,
                ),
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Register Now", fontWeight = FontWeight.SemiBold)
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
            Icon(icon, contentDescription = null, tint = NexusColors.NexusBlue,
                modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
