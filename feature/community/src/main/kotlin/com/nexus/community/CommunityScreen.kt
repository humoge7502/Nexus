package com.nexus.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.GlassCard
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.theme.NexusColors

@Composable
fun CommunityScreen(
    onNavigateToCircle: (String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Community",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }

        item { SectionHeader("Your Circles") }

        item {
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Your Batch", style = MaterialTheme.typography.titleLarge)
                    Text("Connect with classmates", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    NexusButton("View Circle", onClick = { onNavigateToCircle("batch") })
                }
            }
        }

        item { SectionHeader("Find Teammates") }

        item {
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Smart India Hackathon", style = MaterialTheme.typography.titleLarge)
                    Text("Looking for: ML Engineer, UI Designer", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    NexusButton("Request to Join", onClick = {})
                }
            }
        }

        item {
            GlassCard(borderColor = NexusColors.ElectricIndigo.copy(alpha = 0.3f)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("More Community Features", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Mentor discovery, peer accountability, and study groups coming soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
