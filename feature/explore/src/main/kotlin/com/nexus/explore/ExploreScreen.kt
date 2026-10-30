package com.nexus.explore

import androidx.compose.ui.res.stringResource
import com.nexus.explore.R

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.SignalTag
import com.nexus.designsystem.component.SignalTone
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Certification
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Price
import com.nexus.domain.model.UiState

data class ExploreCategory(
    val name: String,
    val icon: ImageVector,
)

private val categories = listOf(
    ExploreCategory(CATEGORY_INTERNSHIPS, Icons.Default.Work),
    ExploreCategory(CATEGORY_HACKATHONS, Icons.Default.EmojiEvents),
    ExploreCategory(CATEGORY_CERTIFICATIONS, Icons.Default.School),
    ExploreCategory(CATEGORY_RESEARCH, Icons.Default.Science),
    ExploreCategory(CATEGORY_OPEN_SOURCE, Icons.Default.Code),
    ExploreCategory(CATEGORY_SCHOLARSHIPS, Icons.Default.Star),
    ExploreCategory(CATEGORY_WORKSHOPS, Icons.Default.MenuBook),
    ExploreCategory(CATEGORY_MORE, Icons.Default.TravelExplore),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    onInternshipClick: (String) -> Unit = {},
    onHackathonClick: (String) -> Unit = {},
    onCertificationClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
    showBackArrow: Boolean = true,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val category = uiState.selectedCategory

    val relevantStates: List<UiState<*>> = when (category) {
        CATEGORY_INTERNSHIPS -> listOf(uiState.internships, uiState.hackathons)
        CATEGORY_HACKATHONS -> listOf(uiState.hackathons)
        CATEGORY_CERTIFICATIONS -> listOf(uiState.certifications)
        CATEGORY_MORE -> listOf(uiState.internships, uiState.hackathons, uiState.certifications)
        else -> listOf(uiState.internships, uiState.hackathons)
    }
    val isLoading = relevantStates.any { it is UiState.Loading }
    val errorMessage = relevantStates.filterIsInstance<UiState.Error>().firstOrNull()?.message
    val totalMatches = uiState.filteredInternships.size +
        uiState.filteredHackathons.size +
        uiState.filteredCertifications.size
    val headerTitle = when (category) {
        CATEGORY_INTERNSHIPS -> "For You — Internships"
        CATEGORY_HACKATHONS -> "For You — Hackathons"
        CATEGORY_CERTIFICATIONS -> "For You — Certifications"
        CATEGORY_MORE -> "For You — Everything"
        else -> "For You — $category"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item {
                ExploreHeader(
                    showBackArrow = showBackArrow,
                    onBack = onBack,
                    resultCount = totalMatches,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }

            item {
                ExploreSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.explore_categories),
                    index = "01",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                )
            }

            item {
                ExploreCategoryChips(
                    selected = category,
                    onSelect = viewModel::selectCategory,
                )
            }

            item {
                SectionHeader(
                    title = headerTitle,
                    index = "02",
                    actionText = if (uiState.hasActiveSearch) stringResource(R.string.explore_clear) else null,
                    onAction = if (uiState.hasActiveSearch) viewModel::clearSearchAndFilters else null,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
                )
            }

            item {
                ExploreFilterChips(
                    selected = uiState.selectedFilters,
                    onToggle = viewModel::toggleFilter,
                )
            }

            exploreResults(
                category = category,
                internships = uiState.filteredInternships,
                hackathons = uiState.filteredHackathons,
                certifications = uiState.filteredCertifications,
                totalMatches = totalMatches,
                isLoading = isLoading,
                errorMessage = errorMessage,
                hasActiveSearch = uiState.hasActiveSearch,
                onRetry = viewModel::refresh,
                onClear = viewModel::clearSearchAndFilters,
                onInternshipClick = onInternshipClick,
                onHackathonClick = onHackathonClick,
                onCertificationClick = onCertificationClick,
            )
        }
    }
}

@Composable
private fun ExploreHeader(
    showBackArrow: Boolean,
    onBack: () -> Unit,
    resultCount: Int,
    modifier: Modifier = Modifier,
) {
    RevealOnLoad {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackArrow) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp).size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.nav_explore),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.explore_results, resultCount),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExploreSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        placeholder = {
            Text(
                stringResource(R.string.explore_search_opportunities),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = NexusShapes.sm,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            unfocusedBorderColor = MaterialTheme.nexus.hairline,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExploreCategoryChips(
    selected: String,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (option in categories) {
            CategoryChip(
                category = option,
                isSelected = selected == option.name,
                onClick = { onSelect(option.name) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExploreFilterChips(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (filter in exploreFilterOptions) {
            NexusChip(
                text = filter.label,
                isSelected = filter.key in selected,
                onToggle = { onToggle(filter.key) },
            )
        }
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.exploreResults(
    category: String,
    internships: List<Internship>,
    hackathons: List<Hackathon>,
    certifications: List<Certification>,
    totalMatches: Int,
    isLoading: Boolean,
    errorMessage: String?,
    hasActiveSearch: Boolean,
    onRetry: () -> Unit,
    onClear: () -> Unit,
    onInternshipClick: (String) -> Unit,
    onHackathonClick: (String) -> Unit,
    onCertificationClick: (String) -> Unit,
) {
    when {
        totalMatches == 0 && isLoading -> loadingRow()
        totalMatches == 0 && errorMessage != null -> errorRow(errorMessage, onRetry)
        totalMatches == 0 -> emptyRow(category, hasActiveSearch, onClear)
        else -> when (category) {
            CATEGORY_INTERNSHIPS -> {
                internshipCards(internships, onInternshipClick)
                if (hackathons.isNotEmpty()) {
                    sectionHeader("Live Hackathons", hackathons.size)
                    hackathonCards(hackathons, onHackathonClick)
                }
            }

            CATEGORY_HACKATHONS -> hackathonCards(hackathons, onHackathonClick)
            CATEGORY_CERTIFICATIONS -> certificationCards(certifications, onCertificationClick)
            CATEGORY_MORE -> {
                if (internships.isNotEmpty()) {
                    sectionHeader("Internships", internships.size)
                    internshipCards(internships, onInternshipClick)
                }
                if (hackathons.isNotEmpty()) {
                    sectionHeader("Hackathons", hackathons.size)
                    hackathonCards(hackathons, onHackathonClick)
                }
                if (certifications.isNotEmpty()) {
                    sectionHeader("Certifications", certifications.size)
                    certificationCards(certifications, onCertificationClick)
                }
            }

            else -> {
                internshipCards(internships, onInternshipClick)
                hackathonCards(hackathons, onHackathonClick)
            }
        }
    }
}

private fun LazyListScope.sectionHeader(title: String, count: Int) {    item {
        SectionHeader(
            title = title,
            meta = stringResource(R.string.explore_label, count),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp),
        )
    }
}

private fun LazyListScope.internshipCards(
    internships: List<Internship>,
    onClick: (String) -> Unit,
) {
    items(internships, key = { "internship_${it.id}" }) { internship ->
        ExploreOpportunityCard(
            title = internship.title,
            org = internship.company.name,
            matchScore = (internship.relevanceScore * 100).toInt(),
            tags = internship.tags.take(3),
            onClick = { onClick(internship.id) },
        )
    }
}

private fun LazyListScope.hackathonCards(
    hackathons: List<Hackathon>,
    onClick: (String) -> Unit,
) {
    items(hackathons, key = { "hackathon_${it.id}" }) { hackathon ->
        HackathonCard(hackathon = hackathon, onClick = { onClick(hackathon.id) })
    }
}

private fun LazyListScope.certificationCards(
    certifications: List<Certification>,
    onClick: (String) -> Unit,
) {
    items(certifications, key = { "certification_${it.id}" }) { certification ->
        CertificationCard(certification = certification, onClick = { onClick(certification.id) })
    }
}

private fun LazyListScope.loadingRow() {
    item {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.explore_loading_opportunities),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.errorRow(message: String, onRetry: () -> Unit) {
    item {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.explore_retry)) }
        }
    }
}

private fun LazyListScope.emptyRow(
    category: String,
    hasActiveSearch: Boolean,
    onClear: () -> Unit,
) {
    item {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                if (hasActiveSearch) {
                    "Nothing in $category matches your search. Try a broader query or clear the filters."
                } else {
                    "No $category listings yet — this feed is refreshed as new opportunities open."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (hasActiveSearch) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onClear) { Text(stringResource(R.string.explore_clear_search_filters)) }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: ExploreCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = NexusShapes.sm
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.nexus.signalContainer
        else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
        label = "catBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.nexus.signal.copy(alpha = 0.55f)
        else MaterialTheme.nexus.hairline,
        label = "catBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.nexus.onSignalContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "catContent",
    )

    Row(
        modifier = Modifier
            .pressScale()
            .clip(shape)
            .background(bgColor)
            .border(NexusSpacing.hairline, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
        )
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        shape = NexusShapes.md,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(NexusShapes.sm)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(org.take(2), style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(org, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (tag in tags.take(2)) {
                        SignalTag(text = tag, tone = SignalTone.Neutral)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            SignalTag(
                text = stringResource(R.string.explore_match_percent, matchScore),
                tone = if (matchScore >= 90) SignalTone.Signal else SignalTone.Neutral,
            )
        }
    }
}

@Composable
private fun CertificationCard(certification: Certification, onClick: () -> Unit) {
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        shape = NexusShapes.md,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(NexusShapes.sm)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    certification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    certification.providerName.ifBlank { certification.provider.displayName },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoPill(
                        text = String.format(java.util.Locale.US, "%.1f", certification.rating),
                        icon = Icons.Default.Star,
                    )
                    InfoPill("${certification.estimatedHours}h")
                    InfoPill(if (certification.price is Price.Free) "Free" else "Paid")
                }
            }
        }
    }
}

@Composable
private fun InfoPill(text: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .clip(NexusShapes.xs)
            .border(
                width = NexusSpacing.hairline,
                color = MaterialTheme.nexus.hairline,
                shape = NexusShapes.xs,
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.nexus.attention,
                modifier = Modifier.size(11.dp),
            )
            Spacer(Modifier.width(3.dp))
        }
        Text(
            text,
            style = NexusTypography.TelemetryMicro,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HackathonCard(hackathon: Hackathon, onClick: () -> Unit) {
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(NexusShapes.sm)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(hackathon.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, maxLines = 1)
                    if (hackathon.isLive) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(11.dp)
                                .clip(NexusShapes.tag)
                                .background(MaterialTheme.nexus.attention),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            stringResource(R.string.explore_live),
                            style = NexusTypography.DataLabel,
                            color = MaterialTheme.nexus.attention,
                        )
                    }
                }
                Text(hackathon.organizer, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Text(stringResource(R.string.explore_prize, hackathon.prizePool),
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.nexus.attention)
            }
        }
    }
}
