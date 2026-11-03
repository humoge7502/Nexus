package com.nexus.internships

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.InstrumentPlate
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Internship
import com.nexus.domain.model.OpportunityKind
import com.nexus.domain.model.UiState
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun InternshipDetailScreen(
    internshipId: String,
    onBack: () -> Unit,
    viewModel: InternshipViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trackedIds by viewModel.trackedIds.collectAsStateWithLifecycle()
    val internships: List<Internship> = (uiState as? UiState.Success<List<Internship>>)?.data
        ?: emptyList()
    val internship = internships.find { it.id == internshipId }
    val scheme = MaterialTheme.colorScheme
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

    if (internship == null) {
        DetailPlaceholder(uiState = uiState, onRetry = viewModel::refresh)
        return
    }

    val deadline = internship.deadline
    val deadlineValue = when {
        deadline == null -> stringResource(R.string.internships_rolling)
        deadline.isBefore(LocalDate.now()) -> stringResource(R.string.internships_closed)
        else -> deadline.format(closingFormat)
    }
    val isClosed = deadline?.isBefore(LocalDate.now()) == true
    val verifiedDate = internship.verifiedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()
        ?.format(verifiedFormat)

    Scaffold(
        containerColor = scheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InternshipDetailTopBar(
                internship = internship,
                onBack = onBack,
                onToggleSave = { viewModel.toggleSave(internship.id) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NexusSpacing.md)
                .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(NexusSpacing.xs))

            IdentityPlate(
                internship = internship,
                deadlineValue = deadlineValue,
                isClosed = isClosed,
            )

            Spacer(Modifier.height(NexusSpacing.sm))

            MetricsPlate(
                internship = internship,
                deadlineValue = deadlineValue,
                isClosed = isClosed,
            )

            ProvenanceLine(internship = internship, verifiedDate = verifiedDate)

            DetailSections(internship = internship)

            Spacer(Modifier.height(NexusSpacing.xl))

            ApplyActions(
                internship = internship,
                isTracked = internship.id in trackedIds,
                isClosed = isClosed,
                onApply = { viewModel.onApplyClicked(internship.id) },
                onTrack = { viewModel.track(internship) },
            )

            Spacer(Modifier.height(NexusSpacing.xl))
        }
    }
}

@Composable
private fun DetailPlaceholder(
    uiState: UiState<List<Internship>>,
    onRetry: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (uiState) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = NexusSpacing.xxl),
                )
                TextButton(onClick = onRetry) {
                    Text(
                        text = stringResource(R.string.internships_retry).uppercase(Locale.ROOT),
                        style = NexusTypography.DataLabel,
                        color = ext.signal,
                    )
                }
            }
            else -> Text(
                stringResource(R.string.internships_this_internship_is_no_longer_available),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

/** Save and share are the only two controls the detail header carries. */
@Composable
private fun InternshipDetailTopBar(
    internship: Internship,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
) {
    val context = LocalContext.current
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme

    NexusTopBar(
        onBack = onBack,
        meta = kindLabel(internship.kind).uppercase(Locale.ROOT),
        actions = {
            NexusIconButton(
                icon = if (internship.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (internship.isSaved) {
                    stringResource(R.string.cd_remove_from_saved)
                } else {
                    stringResource(R.string.cd_save)
                },
                onClick = onToggleSave,
                tint = if (internship.isSaved) ext.signal else scheme.onSurfaceVariant,
            )
            NexusIconButton(
                icon = Icons.Default.Share,
                contentDescription = stringResource(R.string.cd_share),
                onClick = {
                    val shareText = buildString {
                        append(
                            context.getString(
                                R.string.internships_share_text,
                                internship.title,
                                internship.company.name,
                            ),
                        )
                        if (internship.applyUrl.isNotBlank()) append("\n${internship.applyUrl}")
                    }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(
                        Intent.createChooser(send, context.getString(R.string.internships_share_internship)),
                    )
                },
                tint = scheme.onSurfaceVariant,
            )
        },
        title = {
            Text(
                text = internship.company.name.uppercase(Locale.ROOT),
                style = NexusTypography.TelemetryMicro,
                color = ext.signal,
            )
        },
    )
}

@Composable
private fun IdentityPlate(
    internship: Internship,
    deadlineValue: String,
    isClosed: Boolean,
) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme

    InstrumentPlate(
        modifier = Modifier.fillMaxWidth(),
        shape = NexusShapes.lg,
    ) {
        Column(Modifier.fillMaxWidth().padding(NexusSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(NexusShapes.sm)
                        .background(ext.signalContainer)
                        .border(NexusSpacing.hairline, ext.signal.copy(alpha = 0.45f), NexusShapes.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = internship.company.name.take(2).uppercase(Locale.ROOT),
                        style = NexusTypography.DataLabel,
                        color = ext.onSignalContainer,
                    )
                }
                Spacer(Modifier.width(NexusSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = internship.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = scheme.onSurface,
                    )
                    Spacer(Modifier.height(NexusSpacing.xxs))
                    Text(
                        text = internship.company.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(NexusSpacing.md))
            Text(
                text = buildString {
                    append(kindLabel(internship.kind).uppercase(Locale.ROOT))
                    append("  ·  ")
                    append(deadlineValue.uppercase(Locale.ROOT))
                },
                style = NexusTypography.DataLabel,
                color = if (isClosed) ext.attention else ext.signal,
            )
        }
    }
}

@Composable
private fun MetricsPlate(
    internship: Internship,
    deadlineValue: String,
    isClosed: Boolean,
) {
    val ext = MaterialTheme.nexus

    InstrumentPlate(
        modifier = Modifier.fillMaxWidth(),
        shape = NexusShapes.md,
    ) {
        Column(Modifier.fillMaxWidth().padding(NexusSpacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md)) {
                Metric(
                    label = stringResource(R.string.internships_duration),
                    value = internship.durationLabel.ifBlank { internship.duration.formatted() },
                    modifier = Modifier.weight(1f),
                )
                Metric(
                    label = stringResource(R.string.internships_stipend),
                    value = compensation(internship),
                    modifier = Modifier.weight(1f),
                    accent = ext.signal,
                )
            }
            Spacer(Modifier.height(NexusSpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.md)) {
                Metric(
                    label = stringResource(R.string.internships_location),
                    value = internship.location.city.ifBlank { stringResource(R.string.internships_remote) },
                    modifier = Modifier.weight(1f),
                )
                Metric(
                    label = stringResource(R.string.internships_deadline),
                    value = deadlineValue,
                    modifier = Modifier.weight(1f),
                    accent = if (isClosed) ext.attention else null,
                )
            }
        }
    }
}

/** Where the record came from and whether anyone has vouched for it recently. */
@Composable
private fun ProvenanceLine(
    internship: Internship,
    verifiedDate: String?,
) {
    if (verifiedDate == null && internship.sourceName.isBlank()) return

    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme

    Spacer(Modifier.height(NexusSpacing.sm))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (verifiedDate != null) ext.positive else ext.hairlineStrong),
        )
        Spacer(Modifier.width(NexusSpacing.xs))
        Text(
            text = buildString {
                if (verifiedDate != null) {
                    append(
                        stringResource(R.string.internships_verified_on, verifiedDate)
                            .uppercase(Locale.ROOT)
                    )
                }
                if (internship.sourceName.isNotBlank()) {
                    if (isNotEmpty()) append("  ·  ")
                    append(stringResource(R.string.internships_source).uppercase(Locale.ROOT))
                    append(' ')
                    append(internship.sourceName.uppercase(Locale.ROOT))
                }
            },
            style = NexusTypography.TelemetryMicro,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailSections(internship: Internship) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme

    if (internship.description.isNotBlank()) {
        Spacer(Modifier.height(NexusSpacing.lg))
        SectionLabel(stringResource(R.string.text_about_this_internship))
        Spacer(Modifier.height(NexusSpacing.xs))
        Text(
            text = internship.description,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
    }

    val skills = internship.requiredSkills.map { it.name }
    if (skills.isNotEmpty()) {
        Spacer(Modifier.height(NexusSpacing.lg))
        SectionLabel(stringResource(R.string.text_skills_required))
        Spacer(Modifier.height(NexusSpacing.xs))
        TokenRow(items = skills, accent = ext.signal)
    }

    if (internship.tags.isNotEmpty()) {
        Spacer(Modifier.height(NexusSpacing.lg))
        SectionLabel(stringResource(R.string.internships_tags))
        Spacer(Modifier.height(NexusSpacing.xs))
        TokenRow(items = internship.tags, accent = ext.structure)
    }
}

@Composable
private fun ApplyActions(
    internship: Internship,
    isTracked: Boolean,
    isClosed: Boolean,
    onApply: () -> Unit,
    onTrack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val hasApplyUrl = internship.applyUrl.isNotBlank()

    NexusButton(
        text = if (hasApplyUrl) {
            stringResource(
                R.string.internships_apply_cta,
                internship.sourceName.ifBlank { stringResource(R.string.internships_apply_short) },
            )
        } else {
            stringResource(R.string.internships_apply_short)
        }.uppercase(Locale.ROOT),
        onClick = {
            if (hasApplyUrl) uriHandler.openUri(internship.applyUrl)
            onApply()
        },
        enabled = hasApplyUrl,
        shape = NexusShapes.sm,
    )

    if (!isClosed) {
        Spacer(Modifier.height(NexusSpacing.sm))
        NexusOutlineButton(
            text = stringResource(
                if (isTracked) R.string.internships_tracked_cta
                else R.string.internships_track_cta
            ).uppercase(Locale.ROOT),
            onClick = onTrack,
            enabled = !isTracked,
            icon = if (isTracked) Icons.Default.Check else null,
        )
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    Column(modifier) {
        Text(
            text = label.uppercase(Locale.ROOT),
            style = NexusTypography.TelemetryMicro,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(NexusSpacing.xxxs))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = accent ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale.ROOT),
        style = NexusTypography.DataLabel,
        color = MaterialTheme.nexus.signal,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TokenRow(items: List<String>, accent: Color) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
    ) {
        for (item in items) {
            Box(
                modifier = Modifier
                    .clip(NexusShapes.xs)
                    .background(accent.copy(alpha = 0.10f))
                    .border(NexusSpacing.hairline, accent.copy(alpha = 0.35f), NexusShapes.xs)
                    .padding(horizontal = NexusSpacing.sm, vertical = NexusSpacing.xxs),
            ) {
                Text(
                    text = item,
                    style = NexusTypography.DataLabel,
                    color = accent,
                )
            }
        }
    }
}

private fun compensation(internship: Internship): String {
    val stipend = internship.stipend
    if (stipend != null) return stipend.formatted()
    return internship.stipendLabel.ifBlank { "—" }
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

private val closingFormat = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private val verifiedFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
