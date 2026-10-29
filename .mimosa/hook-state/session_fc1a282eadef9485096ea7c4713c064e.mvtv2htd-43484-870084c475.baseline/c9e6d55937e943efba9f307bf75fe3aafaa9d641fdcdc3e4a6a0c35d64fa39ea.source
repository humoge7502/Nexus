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
    const val MIN_TOUCH_TARGET_DP = 44
}

fun Modifier.minimumTouchTarget(size: Dp = 44.dp): Modifier =
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
