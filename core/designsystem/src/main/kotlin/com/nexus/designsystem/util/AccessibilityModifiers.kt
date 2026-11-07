package com.nexus.designsystem.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
object AccessibilityDefaults {
    /** The design direction's floor for every interactive surface. */
    const val MIN_TOUCH_TARGET_DP = 48
}

fun Modifier.minimumTouchTarget(size: Dp = 48.dp): Modifier =
    this.sizeIn(minWidth = size, minHeight = size)

fun Modifier.accessibleClickable(
    contentDescription: String,
    onClick: () -> Unit,
): Modifier = this
    .minimumTouchTarget()
    .clickable(
        onClickLabel = contentDescription,
        onClick = onClick,
    )

fun Modifier.accessibleIconDescription(description: String): Modifier =
    this.semantics { contentDescription = description }
