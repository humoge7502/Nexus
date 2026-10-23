package com.nexus.designsystem.effect

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.effect.BlinkingCursor
import kotlinx.coroutines.flow.Flow

@Composable
fun StreamingAIResponse(
    textFlow: Flow<String>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    var displayedText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(true) }
    LaunchedEffect(textFlow) {
        textFlow.collect { chunk -> displayedText += chunk }
        isStreaming = false
    }
    Column(modifier = modifier) {
        Text(
            text = displayedText,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
        if (isStreaming) BlinkingCursor(modifier = Modifier)
    }
}
