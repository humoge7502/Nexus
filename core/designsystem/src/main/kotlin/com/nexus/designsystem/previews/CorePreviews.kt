package com.nexus.designsystem.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.designsystem.component.MomentumChart
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.TelemetryItem
import com.nexus.designsystem.component.TelemetryStrip
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusTheme
import com.nexus.designsystem.theme.NexusTypography

/**
 * Preview smoketests for the core controls and instrument pieces, in both
 * themes. Preview copy is dev-only by definition — production copy still
 * lives in strings.xml; these exist so a token change is visible in the IDE
 * pane before any build, per the design system's review checklist.
 */

@Composable
private fun ControlBoard() {
    Column(Modifier.padding(16.dp)) {
        SectionHeader(
            title = "Today's readings",
            index = "01",
            meta = "12",
            actionText = "View all",
            onAction = {},
        )
        Spacer(Modifier.height(12.dp))
        NexusButton(text = "Start conversation", onClick = {}, gradient = NexusGradients.PrimaryDeep)
        Spacer(Modifier.height(8.dp))
        NexusButton(text = "Analyze gap", onClick = {})
        Spacer(Modifier.height(8.dp))
        NexusOutlineButton(text = "Track application", onClick = {})
        Spacer(Modifier.height(12.dp))
        NexusTextField(
            value = "",
            onValueChange = {},
            label = "TARGET ROLE",
            placeholder = "e.g. Machine Learning Engineer",
        )
        Spacer(Modifier.height(12.dp))
        NexusCard(Modifier.fillMaxWidth()) {
            Text("A card is the only container a reading needs", style = NexusTypography.Voice)
        }
        Spacer(Modifier.height(12.dp))
        TelemetryStrip(
            items = listOf(
                TelemetryItem("Applications", 4),
                TelemetryItem("Certs", 2),
                TelemetryItem("Day streak", 12),
            ),
        )
    }
}

@Preview(name = "Controls — light", showBackground = true)
@Composable
private fun ControlBoardLight() {
    NexusTheme(darkTheme = false) { ControlBoard() }
}

@Preview(name = "Controls — dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ControlBoardDark() {
    NexusTheme(darkTheme = true) { ControlBoard() }
}

@Composable
private fun InstrumentBoard() {
    Column(Modifier.padding(16.dp)) {
        NexusTopBar(title = "Growth", subtitle = "Sagitta score", meta = "04 NOV")
        Spacer(Modifier.height(12.dp))
        InstrumentGauge(progress = 0.62f, contentDescription = "Preview gauge") {
            Text("62%", style = NexusTypography.Stat)
        }
        Spacer(Modifier.height(12.dp))
        CalibratedRule(progress = 0.45f, contentDescription = "Preview rule")
        Spacer(Modifier.height(12.dp))
        MomentumChart(values = listOf(120f, 180f, 150f, 240f, 210f, 320f))
        Spacer(Modifier.height(12.dp))
        EmptyState(
            title = "No readings yet",
            description = "The momentum line starts drawing once there are two.",
            voice = "One reading is saved a day, while you have the app open.",
        )
    }
}

@Preview(name = "Instruments — light", showBackground = true)
@Composable
private fun InstrumentBoardLight() {
    NexusTheme(darkTheme = false) { InstrumentBoard() }
}

@Preview(name = "Instruments — dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InstrumentBoardDark() {
    NexusTheme(darkTheme = true) { InstrumentBoard() }
}

@Preview(name = "Icon button", showBackground = true)
@Composable
private fun IconButtonPreview() {
    NexusTheme(darkTheme = false) {
        NexusIconButton(
            icon = Icons.Default.Notifications,
            contentDescription = "Notifications",
            onClick = {},
        )
    }
}

@Preview(name = "Chip", showBackground = true)
@Composable
private fun ChipPreview() {
    NexusTheme(darkTheme = true) {
        NexusChip(text = "Internships", isSelected = true, onToggle = {})
    }
}
