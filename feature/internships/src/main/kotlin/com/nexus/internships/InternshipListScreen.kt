package com.nexus.internships

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.InstrumentPlate
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.SignalChoice
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Internship
import com.nexus.domain.model.OpportunityKind
import com.nexus.domain.model.UiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InternshipListScreen(
    onInternshipClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: InternshipViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InternshipEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is InternshipEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is InternshipEffect.ShowLevelUp ->
                    snackbarHostState.showSnackbar("LEVEL ${effect.newLevel} REACHED")
            }
        }
    }

    val allInternships: List<Internship> =
        (uiState as? UiState.Success<List<Internship>>)?.data ?: emptyList()
    val internships: List<Internship> = allInternships.filterInternships(searchQuery, selectedFilters)


    Scaffold(
        topBar = {
            NexusTopBar(
                title = stringResource(R.string.internships_internships),
                subtitle = stringResource(R.string.internships_subtitle),
                meta = stringResource(R.string.internships_feed_meta),
                onBack = onBack,
                actions = {
                    NexusIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh),
                        onClick = viewModel::refresh,
                        tint = MaterialTheme.nexus.signal,
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = NexusShapes.xs,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
        ) {
            NexusTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = stringResource(R.string.internships_search_label),
                placeholder = stringResource(R.string.internships_search_internships),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.nexus.signal,
                        modifier = Modifier.size(18.dp),
                    )
                },
                modifier = Modifier.padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.xs),
            )

            InternshipFilterChips(
                selected = selectedFilters,
                onToggle = { filter ->
                    selectedFilters = if (filter in selectedFilters) {
                        selectedFilters - filter
                    } else {
                        selectedFilters + filter
                    }
                },
            )

            FeedCounter(count = internships.size)

            InternshipFeed(
                loading = uiState is UiState.Loading,
                internships = internships,
                searchQuery = searchQuery,
                feedEmpty = allInternships.isEmpty(),
                filterActive = selectedFilters.isNotEmpty(),
                onClearFilters = { selectedFilters = emptySet() },
                onInternshipClick = onInternshipClick,
                onToggleSave = viewModel::toggleSave,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InternshipFilterChips(
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
    ) {
        internshipFilters.forEach { filter ->
            SignalChoice(
                label = filter,
                selected = filter in selected,
                onClick = { onToggle(filter) },
            )
        }
    }
}

/** Loading, empty and populated states are three different layouts, not one. */
@Composable
private fun InternshipFeed(
    loading: Boolean,
    internships: List<Internship>,
    searchQuery: String,
    feedEmpty: Boolean,
    filterActive: Boolean,
    onClearFilters: () -> Unit,
    onInternshipClick: (String) -> Unit,
    onToggleSave: (String) -> Unit,
) {
    when {
        loading -> Text(
            text = stringResource(R.string.internships_loading_internships).uppercase(Locale.ROOT),
            modifier = Modifier.fillMaxWidth().padding(NexusSpacing.xxl),
            style = NexusTypography.TelemetryMicro,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        internships.isEmpty() -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NexusSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = when {
                    feedEmpty -> stringResource(R.string.internships_empty_feed)
                    searchQuery.isNotBlank() -> stringResource(R.string.internships_no_matches, searchQuery)
                    else -> stringResource(R.string.internships_no_filter_matches)
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!feedEmpty && filterActive) {
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = stringResource(R.string.internships_clear_filters).uppercase(Locale.ROOT),
                        style = NexusTypography.DataLabel,
                        color = MaterialTheme.nexus.signal,
                    )
                }
            }
        }
        else -> LazyColumn(
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            contentPadding = PaddingValues(
                start = NexusSpacing.md,
                end = NexusSpacing.md,
                top = NexusSpacing.xs,
                bottom = NexusSpacing.md,
            )
        ) {
            items(internships, key = { it.id }) { internship ->
                InternshipListItem(
                    internship = internship,
                    onClick = { onInternshipClick(internship.id) },
                    onToggleSave = { onToggleSave(internship.id) },
                )
            }
        }
    }
}

@Composable
private fun FeedCounter(count: Int) {
    val ext = MaterialTheme.nexus
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.internships_results, count).uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = ext.signal,
        )
        Spacer(Modifier.width(NexusSpacing.xs))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(NexusSpacing.hairline)
                .background(ext.hairline),
        )
    }
}

@Composable
private fun InternshipListItem(
    internship: Internship,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme
    val shape = NexusShapes.sm

    InstrumentPlate(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        containerColor = scheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = NexusSpacing.sm, vertical = NexusSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(shape)
                    .background(ext.signalContainer)
                    .border(NexusSpacing.hairline, ext.signal.copy(alpha = 0.45f), shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = internship.company.name.take(2).uppercase(Locale.ROOT),
                    style = NexusTypography.TelemetryMicro,
                    color = ext.onSignalContainer,
                )
            }
            Spacer(modifier = Modifier.width(NexusSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                if (internship.kind != OpportunityKind.INTERNSHIP) {
                    Text(
                        text = kindLabel(internship.kind).uppercase(Locale.ROOT),
                        style = NexusTypography.TelemetryMicro,
                        color = ext.attention,
                    )
                    Spacer(Modifier.height(NexusSpacing.xxxs))
                }
                Text(
                    text = internship.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(NexusSpacing.xxxs))
                Text(
                    text = internship.company.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(NexusSpacing.xxs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                ) {
                    Text(
                        text = internship.location.city.compactLocations(),
                        style = NexusTypography.TelemetryMicro,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    internship.compensationLabel()?.let {
                        Box(
                            modifier = Modifier
                                .width(NexusSpacing.hairline)
                                .height(10.dp)
                                .background(ext.hairlineStrong),
                        )
                        Text(
                            text = it,
                            style = NexusTypography.TelemetryMicro,
                            color = ext.signal,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                Spacer(Modifier.height(NexusSpacing.xxs))
                Text(
                    text = internship.closingLabel(),
                    style = NexusTypography.TelemetryMicro,
                    color = when {
                        internship.isClosed() -> ext.attention
                        internship.deadline == null -> scheme.onSurfaceVariant
                        else -> ext.signal
                    },
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.width(NexusSpacing.xs))
            NexusIconButton(
                icon = if (internship.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = stringResource(
                    if (internship.isSaved) R.string.cd_remove_from_saved else R.string.cd_save
                ),
                onClick = onToggleSave,
                tint = if (internship.isSaved) ext.signal else scheme.onSurfaceVariant,
            )
        }
    }
}

/** "Mumbai / Pune / Hyderabad" collapses to "MUMBAI +2" so the meta line stays on one row. */
private fun String.compactLocations(): String {
    val parts = split('/', ',', '|').map { it.trim() }.filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> uppercase(Locale.ROOT)
        parts.size == 1 -> parts.first().uppercase(Locale.ROOT)
        else -> "${parts.first().uppercase(Locale.ROOT)} +${parts.size - 1}"
    }
}

private val closingFormat = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

private fun Internship.isClosed(): Boolean = deadline?.isBefore(LocalDate.now()) == true

/**
 * The feed publishes compensation as text, not a number, so the list shows the
 * published label unless it only says that no figure was listed.
 */
private fun Internship.compensationLabel(): String? =
    stipend?.formatted()?.uppercase(Locale.ROOT)
        ?: stipendLabel
            .takeIf {
                it.isNotBlank() &&
                    !it.contains("not listed", ignoreCase = true) &&
                    !it.contains("unpaid", ignoreCase = true)
            }
            ?.uppercase(Locale.ROOT)

@Composable
private fun Internship.closingLabel(): String {
    val date = deadline
    return when {
        date == null -> stringResource(R.string.internships_rolling)
        date.isBefore(LocalDate.now()) -> stringResource(R.string.internships_closed)
        else -> stringResource(R.string.internships_closes_on, date.format(closingFormat))
    }.uppercase(Locale.ROOT)
}

@Composable
private fun kindLabel(kind: OpportunityKind): String = stringResource(
    when (kind) {
        OpportunityKind.INTERNSHIP -> R.string.internships_kind_internship
        OpportunityKind.FELLOWSHIP -> R.string.internships_kind_fellowship
        OpportunityKind.SCHOLARSHIP -> R.string.internships_kind_scholarship
        OpportunityKind.PROGRAM -> R.string.internships_kind_program
    }
)
