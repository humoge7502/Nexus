package com.nexus.certifications

import androidx.compose.ui.res.stringResource
import com.nexus.certifications.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.domain.model.Certification
import com.nexus.domain.model.DifficultyLevel
import com.nexus.domain.model.UiState

@Composable
fun CertificationDetailScreen(
    certId: String,
    onBack: () -> Unit,
    viewModel: CertificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cert = (uiState as? UiState.Success<List<Certification>>)?.data?.find { it.id == certId }
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = { },
            onBack = onBack,
        )

        if (cert == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is UiState.Loading -> CircularProgressIndicator()
                    is UiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            (uiState as UiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.certifications_retry)) }
                    }
                    else -> Text(stringResource(R.string.certifications_this_certification_is_no_longer_available),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return
        }

        val progress = cert.userProgress
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            val accent = cert.accentColor()
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(accent, accent.copy(alpha = 0.7f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cert.title.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = cert.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = cert.displayProvider(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatColumn(
                    value = if (cert.rating > 0f) "${cert.rating}" else "—",
                    label = stringResource(R.string.certifications_rating),
                    color = NexusColors.Amber400,
                )
                StatColumn(
                    value = stringResource(R.string.certifications_h, cert.estimatedHours),
                    label = stringResource(R.string.certifications_duration),
                    color = accent,
                )
                StatColumn(
                    value = when (cert.difficulty) {
                        DifficultyLevel.BEGINNER -> "Beginner"
                        DifficultyLevel.INTERMEDIATE -> "Intermediate"
                        DifficultyLevel.ADVANCED -> "Advanced"
                    },
                    label = stringResource(R.string.certifications_level),
                    color = NexusColors.Emerald400,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NexusCard(shape = NexusShapes.md) {
                Column {
                    DetailRow(stringResource(R.string.certifications_provider), cert.displayProvider())
                    DetailRow(stringResource(R.string.certifications_category), cert.category.displayLabel())
                    DetailRow(stringResource(R.string.certifications_price), cert.priceLabel())
                    DetailRow(
                        stringResource(R.string.certifications_estimated_time),
                        stringResource(R.string.certifications_hours, cert.estimatedHours),
                    )
                }
            }

            if (cert.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = stringResource(R.string.text_skills_you_ll_gain),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (skill in cert.skills) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            if (progress != null && progress.totalModules > 0) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = if (progress.isCompleted) {
                                stringResource(R.string.certifications_completed)
                            } else {
                                stringResource(R.string.certifications_your_progress)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.certifications_modules_done, progress.completedModules, progress.totalModules),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${(progress.progressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.progressPercent },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (progress != null && !progress.isCompleted) {
                OutlinedButton(
                    onClick = { viewModel.completeModule(cert.id) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(text = stringResource(R.string.text_mark_done),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
            Button(
                onClick = {
                    if (progress == null) {
                        viewModel.startCertification(cert.id, cert.estimatedHours)
                    }
                    if (cert.enrollmentUrl.isNotBlank()) {
                        uriHandler.openUri(cert.enrollmentUrl)
                    }
                },
                enabled = cert.enrollmentUrl.isNotBlank(),
                modifier = Modifier
                    .weight(if (progress != null && !progress.isCompleted) 1.5f else 1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = when {
                        progress?.isCompleted == true -> stringResource(R.string.certifications_review_course)
                        progress != null -> stringResource(R.string.certifications_continue_course)
                        else -> stringResource(R.string.certifications_start_course)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
