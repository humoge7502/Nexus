package com.nexus.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.common.Constants
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/**
 * The wide-screen halves of one navigation model.
 *
 * [NexusBottomBar] is the compact form. These are the same five destinations at
 * the same widths of meaning when the viewport stops being a phone held in one
 * hand — a rail for a tablet propped on a desk, a permanent drawer for a device
 * wide enough that a thumb should not have to cross it twice.
 *
 * They share [NexusNavItems], the same signal colour, the same 2dp index needle
 * that marks the selected station in the bar. Three layouts of one model is the
 * point; three layouts each inventing their own accent is how adaptive apps end up
 * looking like three apps.
 *
 * Both are built rather than lifted from Material's own rail and drawer, because
 * M3 wraps the selected item in a tonal pill and this system marks selection with a
 * needle and an icon swap, the way its gauges mark a value.
 */

/** Which chrome a viewport of [width]dp should draw. */
enum class NexusNavigationLayout { BottomBars, Rail, PermanentDrawer }

/**
 * Breakpoints at 600dp and 840dp — the window size classes, not device names.
 *
 * Keyed to width alone, so a foldable unfolding picks up the drawer without the app
 * knowing it is on a different device.
 */
fun nexusNavigationLayoutFor(width: Dp): NexusNavigationLayout = when {
    width >= 840.dp -> NexusNavigationLayout.PermanentDrawer
    width >= 600.dp -> NexusNavigationLayout.Rail
    else -> NexusNavigationLayout.BottomBars
}

private const val RailWidth = 84
private const val DrawerWidth = 252

/** A vertical navigation rail for medium viewports. */
@Composable
fun NexusRailBar(
    selectedIndex: Int,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(RailWidth.dp)
            .border(width = NexusSpacing.hairline, color = ext.hairline),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Insets are taken inside the surface, not outside it: the rail
                // paints edge to edge under the status bar, and only its contents
                // move below it.
                .systemBarsPadding()
                .padding(top = NexusSpacing.md, bottom = NexusSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NexusMark(size = 28.dp, contentDescription = null)
            Spacer(Modifier.height(NexusSpacing.xl))
            Column(verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs)) {
                NexusNavItems.forEachIndexed { index, item ->
                    RailStation(
                        item = item,
                        isSelected = index == selectedIndex,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigate(index)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RailStation(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val label = stringResource(item.labelRes)

    Box(
        modifier = Modifier
            .size(width = (RailWidth - 16).dp, height = 60.dp)
            .pressScale()
            .semantics {
                contentDescription = label
                role = Role.Tab
                selected = isSelected
            }
            .clickable(role = Role.Tab) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            // The needle sits on the inner edge: it points into the content, the
            // same mark the bar puts under the active station.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .fillMaxHeight(0.52f)
                    .background(ext.signal),
            )
        }
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = null,
            tint = if (isSelected) ext.signal else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** A permanent drawer for expanded viewports, where a rail would waste the width. */
@Composable
fun NexusSideBar(
    selectedIndex: Int,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    applicationName: String = Constants.APP_NAME,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(DrawerWidth.dp)
            .border(width = NexusSpacing.hairline, color = ext.hairline),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = NexusSpacing.md),
        ) {
            Spacer(Modifier.height(NexusSpacing.xl))
            Row(
                modifier = Modifier.padding(start = NexusSpacing.xxs, bottom = NexusSpacing.xxl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NexusMark(size = 30.dp)
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    text = applicationName,
                    style = NexusTypography.TitleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            NexusNavItems.forEachIndexed { index, item ->
                DrawerStation(
                    item = item,
                    index = index,
                    isSelected = index == selectedIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigate(index)
                    },
                )
            }
        }
    }
}

@Composable
private fun DrawerStation(
    item: BottomNavItem,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val label = stringResource(item.labelRes)
    val shape = NexusShapes.sm

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(NexusSpacing.touchTarget)
            .padding(vertical = NexusSpacing.xxs)
            .clip(shape)
            .background(if (isSelected) ext.signal.copy(alpha = 0.10f) else ext.hairline.copy(alpha = 0.0f))
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .clickable(role = Role.Tab) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight(0.44f)
                .background(if (isSelected) ext.signal else Color.Transparent),
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = null,
            tint = if (isSelected) ext.signal else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(NexusSpacing.md))
        Text(
            text = label,
            style = NexusTypography.TitleSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        // Ordinal in mono, so the drawer reads as a list of stations and a
        // screen-reader-free glance still says which one is live.
        Text(
            text = "${index + 1}",
            style = NexusTypography.TelemetryMicro,
            color = if (isSelected) ext.signal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        )
    }
}
