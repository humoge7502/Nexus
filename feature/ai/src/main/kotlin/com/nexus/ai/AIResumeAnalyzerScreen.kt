package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

private enum class SectionTone { Positive, Neutral, Caution, Critical }

private data class SectionAnalysis(
    val name: String,
    val score: Int,
    val status: String,
    val tone: SectionTone,
)

private val sectionAnalyses = listOf(
    SectionAnalysis("Contact Info", 95, "Excellent", SectionTone.Positive),
    SectionAnalysis("Skills Section", 82, "Strong", SectionTone.Neutral),
    SectionAnalysis("Experience", 55, "Needs Work", SectionTone.Caution),
    SectionAnalysis("Quantified Results", 20, "Missing", SectionTone.Critical),
    SectionAnalysis("Keywords", 65, "Partial Match", SectionTone.Caution),
)

@Composable
fun AIResumeAnalyzerScreen(
    onBack: () -> Unit = {},
) {
    var hasAnalyzed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        NexusTopBar(
            title = stringResource(R.string.ai_resume_analyzer),
            onBack = onBack,
        )

        if (!hasAnalyzed) {
            UploadSection(onAnalyze = { hasAnalyzed = true })
        } else {
            ResultsSection()
        }
    }
}

@Composable
private fun ResultsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        NexusCard(
            gradient = NexusGradients.Primary,
            shape = NexusShapes.lg,
        ) {
            Column {
                Text(
                    stringResource(R.string.ai_ats_score).uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = NexusColors.SignalInk.copy(alpha = 0.86f),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.ai_68),
                    style = NexusTypography.TelemetryDisplay,
                    color = NexusColors.SignalInk,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.ai_needs_improvement),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexusColors.SignalInk.copy(alpha = 0.86f),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.ai_detailed_analysis), style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val nexus = MaterialTheme.nexus
        for (analysis in sectionAnalyses) {
            val toneColor = when (analysis.tone) {
                SectionTone.Positive -> nexus.positive
                SectionTone.Neutral -> MaterialTheme.colorScheme.primary
                SectionTone.Caution -> nexus.attention
                SectionTone.Critical -> MaterialTheme.colorScheme.error
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(analysis.name, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.ai_score_percent, analysis.score), style = MaterialTheme.typography.labelSmall,
                            color = toneColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { analysis.score / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(NexusShapes.tag),
                        color = toneColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadSection(onAnalyze: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NexusCard(
            modifier = Modifier.fillMaxWidth(),
            shape = NexusShapes.lg,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null,
                        modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.ai_upload_your_resume), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.ai_pdf_format_supported), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(NexusShapes.sm)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAnalyze() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.ai_choose_file), style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.ai_or_paste_your_resume_text_below),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
