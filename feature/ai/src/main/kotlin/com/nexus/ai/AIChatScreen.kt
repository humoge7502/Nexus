package com.nexus.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nexus.data.repository.AIConversationRepository
import com.nexus.designsystem.effect.IntelligencePulse
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    repository: AIConversationRepository? = null,
    onBack: () -> Unit = {},
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("ai_0", "Hey there! I'm your NEXUS AI career mentor. What would you like help with today?", false)
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        messages.add(ChatMessage("u_${System.currentTimeMillis()}", text, true))
        inputText = ""
        keyboardController?.hide()
        isThinking = true

        scope.launch {
            val reply = if (repository != null) {
                val convId = repository.createConversation()
                val result = repository.sendMessage(convId, text)
                result.dataOrNull ?: getMockResponse(text)
            } else {
                getMockResponse(text)
            }
            messages.add(ChatMessage("ai_${System.currentTimeMillis()}", reply, false))
            isThinking = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape)
                        .background(Brush.linearGradient(NexusGradients.Nexus)),
                        contentAlignment = Alignment.Center) {
                        Text("\u2728", style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("NEXUS AI Mentor", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall)
                        Text("Powered by OpenRouter", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(messages) { msg ->
                MessageBubble(message = msg)
            }
            if (isThinking) {
                item {
                    Row {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(Brush.linearGradient(NexusGradients.Nexus)),
                            contentAlignment = Alignment.Center) {
                            IntelligencePulse(isThinking = true, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)) {
                            Text("Thinking...", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask NEXUS anything...") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { sendMessage(inputText) },
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(Brush.linearGradient(NexusGradients.Nexus)),
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Review my resume", "Best certs for me", "Mock interview", "Improve LinkedIn").forEach { prompt ->
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(NexusColors.Indigo500.copy(alpha = 0.1f))
                    .clickable { sendMessage(prompt) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(prompt, style = MaterialTheme.typography.labelSmall,
                        color = NexusColors.Indigo400)
                }
            }
        }
    }
}

internal fun getMockResponse(input: String): String = when {
    input.contains("resume", ignoreCase = true) ->
        "I'd be happy to review your resume! Paste the text here and I'll analyze it for ATS compatibility, missing keywords, and improvement suggestions."
    input.contains("cert", ignoreCase = true) ->
        "Based on your profile, I recommend: 1) AWS Cloud Practitioner (8 hrs), 2) Google Data Analytics (40 hrs), 3) Meta Frontend (36 hrs)."
    input.contains("interview", ignoreCase = true) ->
        "Let's do a mock interview! First question: 'Tell me about a time you had to learn a new technology quickly.'"
    input.contains("linkedin", ignoreCase = true) ->
        "Here's a LinkedIn post draft: 'Thrilled to share that I've completed my AWS Cloud Practitioner certification!'"
    input.contains("plan", ignoreCase = true) || input.contains("roadmap", ignoreCase = true) ->
        "Here's your 30-Day System Design Plan: Week 1: Fundamentals, Week 2: Core Patterns, Week 3: Advanced, Week 4: Practice."
    else ->
        "Great question! Based on your profile, I'd suggest focusing on building a strong portfolio with 2-3 full-stack projects. Would you like me to dive deeper?"
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start) {
            if (!message.isUser) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(Brush.linearGradient(NexusGradients.Nexus)),
                    contentAlignment = Alignment.Center) {
                    Text("\u2728", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.width(8.dp))
            }
            Box(modifier = Modifier.fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(
                    topStart = if (message.isUser) 16.dp else 4.dp,
                    topEnd = if (message.isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(if (message.isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp)) {
                Text(message.text, style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White
                        else MaterialTheme.colorScheme.onSurface)
            }
            if (message.isUser) {
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center) {
                    Text("U", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
