package com.nexus.certifications

import androidx.compose.ui.res.stringResource
import com.nexus.certifications.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.CertProvider
import com.nexus.domain.model.Certification
import com.nexus.domain.model.DifficultyLevel
import com.nexus.domain.model.Price
import com.nexus.domain.model.SkillCategory
import com.nexus.domain.model.UiState
import java.util.Locale

internal fun CertProvider.accentColor(): Color = when (this) {
    CertProvider.GOOGLE -> Color(0xFF4285F4)
    CertProvider.META -> Color(0xFF1877F2)
    CertProvider.AWS -> Color(0xFFFF9900)
    CertProvider.MICROSOFT -> Color(0xFF0078D4)
    CertProvider.IBM -> Color(0xFF0F62FE)
    CertProvider.COURSERA -> Color(0xFF0056D2)
    CertProvider.UDEMY -> Color(0xFF5624D0)
    CertProvider.LINKEDIN -> Color(0xFF0A66C2)
    CertProvider.OTHER -> Color(0xFF4F8EF7)
}

internal fun Certification.accentColor(): Color {
    if (provider != CertProvider.OTHER) return provider.accentColor()
    val label = providerName.lowercase()
    return when {
        "google" in label -> CertProvider.GOOGLE.accentColor()
        "meta" in label || "facebook" in label -> CertProvider.META.accentColor()
        "aws" in label || "amazon" in label -> CertProvider.AWS.accentColor()
        "microsoft" in label || "azure" in label -> CertProvider.MICROSOFT.accentColor()
        "ibm" in label -> CertProvider.IBM.accentColor()
        "coursera" in label -> CertProvider.COURSERA.accentColor()
        "udemy" in label -> CertProvider.UDEMY.accentColor()
        "linkedin" in label -> CertProvider.LINKEDIN.accentColor()
        else -> CertProvider.OTHER.accentColor()
    }
}

internal fun Certification.displayProvider(): String =
    providerName.ifBlank { provider.displayName }

internal fun Certification.priceLabel(): String = when (val p = price) {
    is Price.Paid -> "Paid · ${p.currency} ${p.amount}"
    Price.Free -> "Free"
}

internal fun SkillCategory.displayLabel(): String = when (this) {
    SkillCategory.AI_ML -> "AI / ML"
    SkillCategory.UI_UX -> "UI / UX"
    else -> name.lowercase()
        .split('_')
        .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
}

/** Three-letter code so the difficulty tag reads as a data field, not a badge. */
internal fun DifficultyLevel.shortCode(): String = when (this) {
    DifficultyLevel.BEGINNER -> "BEG"
    DifficultyLevel.INTERMEDIATE -> "INT"
    DifficultyLevel.ADVANCED -> "ADV"
}

@Composable
fun CertificationBrowserScreen(
    onBack: () -> Unit,
    onCertClick: (String) -> Unit,
    viewModel: CertificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val certifications = (uiState as? UiState.Success<List<Certification>>)?.data.orEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.certifications_certifications),
            onBack = onBack,
        )

        when {
            certifications.isNotEmpty() -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            ) {
                val featured = certifications.sortedByDescending { it.rating }.take(6)
                if (featured.isNotEmpty()) {
                    item {
                        Text(text = stringResource(R.string.text_featured),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(featured, key = { it.id }) { cert ->
                                FeaturedCertCard(cert = cert, onClick = { onCertClick(cert.id) })
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.certifications_all_certifications, certifications.size),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp),
                    )
                }

                items(certifications, key = { it.id }) { cert ->
                    CertListItem(cert = cert, onClick = { onCertClick(cert.id) })
                }
            }

            uiState is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            uiState is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        (uiState as UiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.certifications_retry)) }
                }
            }

            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.certifications_no_certifications_available_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FeaturedCertCard(
    cert: Certification,
    onClick: () -> Unit,
) {
    val identity = lerp(cert.accentColor(), Color.White, 0.35f)
    NexusCard(
        onClick = onClick,
        modifier = Modifier.width(240.dp),
        gradient = NexusGradients.Cosmic,
        shape = NexusShapes.md,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    )
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = NexusColors.Bone.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = cert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NexusColors.Bone,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = NexusColors.Bone.copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (cert.rating > 0f) "${cert.rating}" else stringResource(R.string.certifications_new),
                        style = NexusTypography.Telemetry,
                        color = NexusColors.Bone,
                    )
                }
                Text(
                    text = stringResource(R.string.certifications_hours_price, cert.estimatedHours, cert.priceLabel()),
                    style = NexusTypography.DataLabel,
                    color = NexusColors.Bone.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun CertListItem(
    cert: Certification,
    onClick: () -> Unit,
) {
    val accent = cert.accentColor()
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(NexusShapes.sm)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cert.title.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = cert.displayProvider(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (cert.rating > 0f) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.nexus.attention,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(stringResource(R.string.certifications_label, cert.rating), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(stringResource(R.string.certifications_h, cert.estimatedHours),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
