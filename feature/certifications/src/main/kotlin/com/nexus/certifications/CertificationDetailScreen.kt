package com.nexus.certifications

import androidx.compose.ui.res.stringResource
import com.nexus.certifications.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.Readout
import com.nexus.designsystem.component.SignalTag
import com.nexus.designsystem.component.SignalTone
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.domain.model.Certification
import com.nexus.domain.model.Price
import com.nexus.domain.model.UiState
import java.util.Locale

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
            // The browser shows featured certifications as dark instrument
            // panels; the detail hero is the same panel opened up. A provider
            // identity tick keeps the accent where it carries meaning, and every
            // figure sits in bone so the reading never depends on which brand
            // colour a provider happens to own.
            val identity = lerp(cert.accentColor(), Color.White, 0.35f)
            NexusCard(
                modifier = Modifier.fillMaxWidth(),
                gradient = NexusGradients.Cosmic,
                shape = NexusShapes.lg,
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(11.dp)
                                .clip(NexusShapes.tag)
                                .background(identity),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = cert.displayProvider().uppercase(Locale.ROOT),
                            style = NexusTypography.DataLabel,
                            color = NexusColors.Bone.copy(alpha = 0.68f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.weight(1f))
                        SignalTag(
                            text = cert.difficulty.shortCode(),
                            tone = SignalTone.Neutral,
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(NexusShapes.sm)
                                .background(NexusColors.Bone.copy(alpha = 0.06f))
                                .border(1.dp, NexusColors.Bone.copy(alpha = 0.14f), NexusShapes.sm),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = cert.title.take(2).uppercase(Locale.ROOT),
                                style = NexusTypography.TelemetryLarge,
                                color = NexusColors.Bone,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = cert.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = NexusColors.Bone,
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NexusColors.Bone.copy(alpha = 0.10f)),
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Readout(
                            value = if (cert.rating > 0f) "${cert.rating}" else "—",
                            label = stringResource(R.string.certifications_rating),
                            valueColor = NexusColors.Bone,
                            labelColor = NexusColors.Bone.copy(alpha = 0.55f),
                            modifier = Modifier.weight(1f),
                        )
                        Readout(
                            value = "${cert.estimatedHours}",
                            label = stringResource(R.string.certifications_duration),
                            unit = "h",
                            valueColor = NexusColors.Bone,
                            labelColor = NexusColors.Bone.copy(alpha = 0.55f),
                            modifier = Modifier.weight(1f),
                        )
                        Readout(
                            value = when (val price = cert.price) {
                                is Price.Paid -> "${price.amount}"
                                Price.Free -> "FREE"
                            },
                            label = stringResource(R.string.certifications_price),
                            unit = (cert.price as? Price.Paid)?.currency,
                            valueColor = NexusColors.Bone,
                            labelColor = NexusColors.Bone.copy(alpha = 0.55f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
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
                                .clip(NexusShapes.xs)
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
                        style = NexusTypography.TelemetryLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // One graduation per module: the tick field is the module count,
                // so the rule reads as "how far through this certification".
                CalibratedRule(
                    progress = progress.progressPercent,
                    divisions = progress.totalModules,
                    accent = MaterialTheme.colorScheme.primary,
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
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = NexusShapes.sm,
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
                    .height(52.dp),
                shape = NexusShapes.sm,
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
