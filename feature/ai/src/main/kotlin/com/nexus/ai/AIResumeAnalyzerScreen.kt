package com.nexus.ai

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes

data class SectionAnalysis(
    val name: String,
    val score: Int,
    val status: String,
    val color: Color,
)

private val sectionAnalyses = listOf(
    SectionAnalysis("Contact Info", 95, "Excellent", NexusColors.AuroraGreen),
    SectionAnalysis("Skills Section", 82, "Strong", NexusColors.NexusBlue),
    SectionAnalysis("Experience", 55, "Needs Work", NexusColors.PlasmaOrange),
    SectionAnalysis("Quantified Results", 20, "Missing", Color(0xFFEF4444)),
    SectionAnalysis("Keywords", 65, "Partial Match", NexusColors.Amber400),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIResumeAnalyzerScreen(
    onBack: () -> Unit = {},
) {
    var hasAnalyzed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(
            title = { Text("Resume Analyzer", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
        NexusCard(gradient = NexusGradients.Primary) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ATS Score", color = Color.White.copy(alpha = 0.8f))
                Text("68", style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text("Needs Improvement", color = Color.White.copy(alpha = 0.8f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Detailed Analysis", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        for (analysis in sectionAnalyses) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(analysis.name, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                        Text("${analysis.score}%", style = MaterialTheme.typography.labelSmall,
                            color = analysis.color, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { analysis.score / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = analysis.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
            glassEffect = true,
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
                Text("Upload Your Resume", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("PDF format supported", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(NexusGradients.Nexus))
                        .clickable { onAnalyze() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Choose File", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text("or paste your resume text below",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
