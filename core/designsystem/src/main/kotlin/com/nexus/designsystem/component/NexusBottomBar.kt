package com.nexus.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.R
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.nexus

data class BottomNavItem(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(R.string.nav_explore, Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    BottomNavItem(R.string.nav_ai, Icons.Filled.Bolt, Icons.Outlined.Bolt),
    BottomNavItem(R.string.nav_growth, Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp),
    BottomNavItem(R.string.nav_profile, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
)

/**
 * Navigation as a single machined part: the lit lens is one object that glides
 * between equal-width stations, rather than five separate pills lighting up in
 * place. A 2dp index needle at the lens foot is the same mark the gauges use,
 * so the chrome and the data share one measurement language.
 *
 * The glide is a near-critically damped spring — it settles without overshoot,
 * because a carriage that bounces reads as a toy, not an instrument.
 */
@Composable
fun NexusBottomBar(
    selectedIndex: Int,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val extended = MaterialTheme.nexus
    val reducedMotion = LocalReducedMotion.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Scaffold's bottomBar slot does not apply window insets itself,
            // so the bar would otherwise sit under the system gesture bar.
            .navigationBarsPadding()
            .padding(
                start = NexusSpacing.gutter,
                end = NexusSpacing.gutter,
                top = NexusSpacing.xs,
                bottom = 10.dp,
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = NexusShapes.lg,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(NexusSpacing.hairline, extended.hairline)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                val count = bottomNavItems.size
                val cellWidth = maxWidth / count
                val lensOffset by animateDpAsState(
                    targetValue = cellWidth * selectedIndex,
                    animationSpec = if (reducedMotion) {
                        snap<Dp>()
                    } else {
                        spring<Dp>(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        )
                    },
                    label = "navLens",
                )

                Canvas(modifier = Modifier.matchParentSize()) {
                    if (selectedIndex < 0) return@Canvas
                    val inset = 1.5.dp.toPx()
                    val lensWidth = cellWidth.toPx() - inset * 2
                    val topLeft = Offset(lensOffset.toPx() + inset, inset)
                    val lensSize = Size(lensWidth, size.height - inset * 2)
                    // Matches NexusShapes.sm: a plate, not a pill.
                    val radius = CornerRadius(10.dp.toPx())

                    drawRoundRect(
                        color = extended.signalContainer,
                        topLeft = topLeft,
                        size = lensSize,
                        cornerRadius = radius,
                    )
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(extended.signal.copy(alpha = 0.10f), Color.Transparent),
                            startY = 0f,
                            endY = size.height,
                        ),
                        topLeft = topLeft,
                        size = lensSize,
                        cornerRadius = radius,
                    )
                    drawRoundRect(
                        color = extended.signal.copy(alpha = 0.55f),
                        topLeft = topLeft,
                        size = lensSize,
                        cornerRadius = radius,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = NexusSpacing.hairline.toPx()
                        ),
                    )

                    val needleWidth = 14.dp.toPx()
                    val needleHeight = 2.dp.toPx()
                    drawRoundRect(
                        color = extended.signal,
                        topLeft = Offset(
                            lensOffset.toPx() + (cellWidth.toPx() - needleWidth) / 2f,
                            size.height - inset - 3.dp.toPx() - needleHeight,
                        ),
                        size = Size(needleWidth, needleHeight),
                        cornerRadius = CornerRadius(needleHeight / 2f),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavStation(
                            item = item,
                            isSelected = index == selectedIndex,
                            onClick = {
                                if (index != selectedIndex) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigate(index)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavStation(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val extended = MaterialTheme.nexus
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.94f,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "navIconScale",
    )
    val tint by animateColorAsState(
        targetValue = if (isSelected) {
            extended.onSignalContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "navTint",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(NexusShapes.sm)
            .selectable(
                selected = isSelected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            // Keeps the label clear of the index needle at the lens foot.
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = stringResource(item.labelRes),
            tint = tint,
            modifier = Modifier
                .size(21.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.3.sp,
            ),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
