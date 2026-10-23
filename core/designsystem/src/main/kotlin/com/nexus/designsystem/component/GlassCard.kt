package com.nexus.designsystem.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.nexus.designsystem.theme.NexusShapes

@Composable
fun GlassCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    shape: Shape = NexusShapes.md,
    content: @Composable BoxScope.() -> Unit,
) = NexusCard(
    onClick = onClick,
    modifier = modifier,
    glassEffect = true,
    shape = shape,
    content = content,
)
