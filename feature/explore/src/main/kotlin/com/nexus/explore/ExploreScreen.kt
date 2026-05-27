package com.nexus.explore

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Company
import com.nexus.domain.model.Location
import com.nexus.domain.model.Stipend
import com.nexus.domain.model.Duration
import java.time.LocalDate

data class ExploreCategory(
    val emoji: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
)

private val categories = listOf(
    ExploreCategory("\uD83C\uDFE2", "Internships", Icons.Default.Work, Color(0xFF4F8EF7)),
    ExploreCategory("\uD83C\uDFC6", "Hackathons", Icons.Default.EmojiEvents, Color(0xFF7B5EA7)),
    ExploreCategory("\uD83D\uDCDC", "Certifications", Icons.Default.School, Color(0xFF00D9A3)),
    ExploreCategory("\uD83D\uDD2C", "Research", Icons.Default.Science, Color(0xFFFF6B35)),
    ExploreCategory("\uD83C\uDF10", "Open Source", Icons.Default.Code, Color(0xFFF72585)),
    ExploreCategory("\uD83C\uDF93", "Scholarships", Icons.Default.Star, Color(0xFFF59E0B)),
    ExploreCategory("\uD83D\uDCBB", "Workshops", Icons.Default.MenuBook, Color(0xFF06B6D4)),
    ExploreCategory("\u2795", "More", Icons.Default.TravelExplore, Color(0xFF6366F1)),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    onInternshipClick: (String) -> Unit = {},
    onHackathonClick: (String) -> Unit = {},
    onCertificationClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Internships") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }

    val filters = listOf("Remote", "Paid", "AI/ML", "Web", "Mobile", "Cloud")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        item {
            TopAppBar(
                title = { Text("Explore", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TravelExplore, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (searchQuery.isEmpty()) "Search opportunities..." else searchQuery,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (category in categories) {
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "For You — $selectedCategory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (filter in filters) {
                    NexusChip(
                        text = filter,
                        isSelected = filter in selectedFilters,
                        onToggle = {
                            selectedFilters = if (filter in selectedFilters)
                                selectedFilters - filter else selectedFilters + filter
                        },
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        items(sampleExploreItems) { item ->
            ExploreOpportunityCard(
                title = item.title,
                org = item.org,
                matchScore = item.matchScore,
                tags = item.tags,
                onClick = { onInternshipClick(item.id) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Live Hackathons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(sampleHackathons) { hackathon ->
            HackathonCard(hackathon = hackathon, onClick = { onHackathonClick(hackathon.id) })
        }
    }
}

@Composable
private fun CategoryChip(
    category: ExploreCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) category.color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "catBg",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = category.emoji, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(6.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ExploreOpportunityCard(
    title: String,
    org: String,
    matchScore: Int,
    tags: List<String>,
    onClick: () -> Unit,
) {
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = NexusShapes.md,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(org.take(2), style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(org, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (tag in tags.take(2)) {
                        Box(Modifier.clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(tag, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.clip(CircleShape)
                    .background(if (matchScore >= 90) NexusGradients.Success[0].copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
            ) {
                Text("${matchScore}%", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (matchScore >= 90) NexusGradients.Success[0]
                        else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HackathonCard(hackathon: Hackathon, onClick: () -> Unit) {
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        glassEffect = true,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(NexusGradients.Stellar[0].copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "\uD83C\uDFC6", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(hackathon.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, maxLines = 1)
                    if (hackathon.isLive) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("LIVE", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
                Text(hackathon.organizer, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Prize: ${hackathon.prizePool}", style = MaterialTheme.typography.labelSmall,
                    color = NexusColors.StellarPink, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private data class ExploreItem(
    val id: String, val title: String, val org: String,
    val matchScore: Int, val tags: List<String>,
)

private val sampleExploreItems = listOf(
    ExploreItem("e1", "Google STEP Internship", "Google", 98, listOf("Python", "DSA")),
    ExploreItem("e2", "Microsoft Learn Intern", "Microsoft", 91, listOf("Cloud", "Azure")),
    ExploreItem("e3", "Amazon SDE Intern", "Amazon", 87, listOf("Java", "System Design")),
    ExploreItem("e4", "Flipkart GRiD", "Flipkart", 85, listOf("Web", "Mobile")),
    ExploreItem("e5", "Goldman Sachs Intern", "Goldman Sachs", 82, listOf("Finance", "DSA")),
    ExploreItem("e6", "Stripe Backend Intern", "Stripe", 79, listOf("Go", "APIs")),
)

private val sampleHackathons = listOf(
    Hackathon("h1", "Smart India Hackathon", "SIH", mode = com.nexus.domain.model.EventMode.HYBRID,
        startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(2),
        registrationDeadline = LocalDate.now().plusDays(3), prizePool = "₹1,00,000",
        registrationUrl = "https://sih.gov.in", isLive = true,
        tags = kotlinx.collections.immutable.persistentListOf("AI", "Web")),
    Hackathon("h2", "MLH Hackathon", "Major League Hacking", mode = com.nexus.domain.model.EventMode.ONLINE,
        startDate = LocalDate.now().plusDays(5), endDate = LocalDate.now().plusDays(7),
        registrationDeadline = LocalDate.now().plusDays(1), prizePool = "$5,000",
        registrationUrl = "https://mlh.io", isLive = true,
        tags = kotlinx.collections.immutable.persistentListOf("All")),
)
