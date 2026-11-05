package com.nexus.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.common.toFormattedDate
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusToastData
import com.nexus.designsystem.component.NexusToastHost
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.ToastTone
import com.nexus.designsystem.component.rememberNexusToastState
import com.nexus.designsystem.component.conversation.AssistantResponse
import com.nexus.designsystem.component.conversation.ConversationByline
import com.nexus.designsystem.component.conversation.ConversationComposer
import com.nexus.designsystem.component.conversation.PromptSuggestionRow
import com.nexus.designsystem.component.conversation.ResponseCallout
import com.nexus.designsystem.component.conversation.UserMessage
import com.nexus.designsystem.component.conversation.parseAssistantResponse
import com.nexus.designsystem.effect.IntelligencePulse
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.AIConversation
import com.nexus.domain.usecase.WarningSeverity
import java.util.Locale

@Composable
fun AIChatScreen(
    onBack: () -> Unit = {},
    viewModel: AIChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberNexusToastState()
    var inputText by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current

    val mentorRole = stringResource(R.string.ai_role_mentor)
    val userRole = stringResource(R.string.ai_role_you)
    val noticeLabel = stringResource(R.string.ai_label_notice)
    val cautionLabel = stringResource(R.string.ai_label_caution)

    LaunchedEffect(uiState.messages.size, uiState.streamingText) {
        val itemCount = uiState.messages.size + if (uiState.streamingText != null) 1 else 0
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    // A limit is not a failure — nothing broke — but the user cannot send
    // another message, so it reads on the ember Attention bar, not a neutral one.
    // getString rather than stringResource: this runs in a coroutine, where
    // composition-only calls are not available.
    val resources = LocalContext.current.resources
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AIChatEffect.ShowRateLimitDialog -> toastState.show(
                    NexusToastData(
                        if (effect.isPro) {
                            resources.getString(R.string.ai_chat_limit_pro)
                        } else {
                            resources.getString(R.string.ai_chat_limit_free, effect.limit)
                        },
                        ToastTone.Attention,
                    ),
                )
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || uiState.isThinking) return
        viewModel.sendMessage(text.trim())
        inputText = ""
        keyboardController?.hide()
    }

    if (showHistory) {
        // A thread is opened in Room the moment the first message is sent, so an
        // exchange that never got an answer leaves a row with nothing in it. The
        // sheet promises saved conversations, so it lists the ones that hold
        // something - and selecting the thread already on screen reloads it from
        // the database, which would drop the turn still in memory.
        ConversationHistorySheet(
            conversations = uiState.conversations.filter { it.messages.isNotEmpty() },
            currentId = uiState.currentConversationId,
            onSelect = { id ->
                showHistory = false
                if (id != uiState.currentConversationId) viewModel.loadConversation(id)
            },
            onNew = {
                showHistory = false
                viewModel.createConversation()
            },
            onDismiss = { showHistory = false },
        )
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexusTopBar(
                onBack = onBack,
                title = stringResource(R.string.ai_nexus_ai_mentor),
                subtitle = stringResource(R.string.ai_personalized_to_your_career_profile),
                meta = stringResource(R.string.ai_link_meta),
                actions = {
                    NexusIconButton(
                        icon = Icons.Default.History,
                        contentDescription = stringResource(R.string.ai_chat_history),
                        onClick = { showHistory = true },
                    )
                },
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    start = NexusSpacing.gutter,
                    end = NexusSpacing.gutter,
                    top = NexusSpacing.lg,
                    bottom = NexusSpacing.md,
                ),
                // An answer is a document, not a line, so turns need room to
                // end before the next speaker starts.
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.xl, Alignment.Bottom),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ConversationTurn(
                        message = message,
                        mentorRole = mentorRole,
                        userRole = userRole,
                        noticeLabel = noticeLabel,
                        cautionLabel = cautionLabel,
                        onCopy = { clipboardManager.setText(AnnotatedString(message.text)) },
                    )
                }
                uiState.streamingText?.let { partial ->
                    item(key = "streaming") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AssistantResponse(
                                blocks = parseAssistantResponse(partial),
                                byline = mentorRole,
                                isStreaming = true,
                            )
                        }
                    }
                }
                if (uiState.isThinking && uiState.streamingText == null) {
                    item(key = "thinking") {
                        ThinkingIndicator(byline = mentorRole)
                    }
                }
            }

            ConversationComposer(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = { sendMessage(inputText) },
                label = stringResource(R.string.ai_message_label),
                placeholder = stringResource(R.string.ai_ask_nexus_anything),
                enabled = !uiState.isThinking,
            )

            PromptSuggestionRow(
                prompts = listOf(
                    stringResource(R.string.ai_prompt_review_resume),
                    stringResource(R.string.ai_prompt_best_certs),
                    stringResource(R.string.ai_prompt_mock_interview),
                    stringResource(R.string.ai_prompt_improve_linkedin),
                ),
                label = stringResource(R.string.ai_suggested),
                enabled = !uiState.isThinking,
                onPrompt = { sendMessage(it) },
                modifier = Modifier.padding(
                    start = NexusSpacing.gutter,
                    end = NexusSpacing.gutter,
                    bottom = NexusSpacing.sm,
                ),
            )
        }

        // The toast host owns its own gutter, so the overlay needs only the
        // bottom-center anchor the old SnackbarHost was given.
        NexusToastHost(
            state = toastState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * One turn of the conversation.
 *
 * The mentor is typeset; the user gets the container. Errors and validator
 * warnings are rendered rather than dropped — an answer that claims a salary
 * figure or guarantees an outcome has to say so on screen.
 */
@Composable
private fun ConversationTurn(
    message: ChatMessage,
    mentorRole: String,
    userRole: String,
    noticeLabel: String,
    cautionLabel: String,
    onCopy: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = MaterialTheme.nexus
    val reduced = LocalReducedMotion.current

    AnimatedVisibility(
        visible = true,
        enter = if (reduced) {
            fadeIn()
        } else {
            fadeIn(tween(motionMillis(NexusAnimation.Duration.standard))) +
                slideInVertically(
                    animationSpec = tween(motionMillis(NexusAnimation.Duration.standard)),
                    initialOffsetY = { it / 3 },
                )
        },
    ) {
        when {
            message.isUser -> UserMessage(text = message.text, byline = userRole)

            message.isError -> Column(modifier = Modifier.fillMaxWidth()) {
                ConversationByline(role = mentorRole)
                Spacer(Modifier.height(NexusSpacing.sm))
                ResponseCallout(
                    label = noticeLabel,
                    // The ViewModel raises this case and the copy lives here, so
                    // the sentence a student reads is translatable.
                    text = message.text.ifBlank { stringResource(R.string.ai_service_unreachable) },
                    tone = scheme.error,
                )
            }

            else -> Column(modifier = Modifier.fillMaxWidth()) {
                AssistantResponse(
                    blocks = parseAssistantResponse(message.text),
                    byline = mentorRole,
                    onCopy = onCopy,
                )
                message.warnings.forEachIndexed { index, warning ->
                    Spacer(Modifier.height(if (index == 0) NexusSpacing.sm else NexusSpacing.xs))
                    ResponseCallout(
                        label = cautionLabel,
                        text = warning.message,
                        tone = when (warning.severity) {
                            WarningSeverity.HIGH -> scheme.error
                            WarningSeverity.MEDIUM -> ext.attention
                            else -> ext.signal
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator(byline: String) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme
    val shape = NexusShapes.sm

    Column(modifier = Modifier.fillMaxWidth()) {
        ConversationByline(role = byline)
        Spacer(Modifier.height(NexusSpacing.sm))
        Row(
            modifier = Modifier
                .clip(shape)
                .background(scheme.surfaceContainerLow)
                .border(NexusSpacing.hairline, ext.hairlineStrong, shape)
                .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntelligencePulse(isThinking = true, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(NexusSpacing.sm))
            Text(
                text = stringResource(R.string.ai_mentor_processing),
                style = NexusTypography.DataLabel,
                color = ext.signal,
            )
        }
    }
}

/**
 * The threads the device already holds.
 *
 * `loadConversation` and `createConversation` have been on this view model since
 * the screen was written, and nothing on the screen could reach either one - so
 * every visit started a fresh thread while the saved ones sat unread in Room.
 */
@Composable
private fun ConversationHistorySheet(
    conversations: List<AIConversation>,
    currentId: String?,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = NexusSpacing.md),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = NexusShapes.xl,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(NexusSpacing.xl),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.ai_chat_history_title)
                                    .uppercase(Locale.ROOT),
                                style = NexusTypography.DataLabel,
                                color = MaterialTheme.nexus.signal,
                            )
                            Spacer(Modifier.height(NexusSpacing.xxs))
                            Text(
                                text = stringResource(R.string.ai_chat_history_subtitle),
                                style = NexusTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        NexusIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.ai_chat_close),
                            onClick = onDismiss,
                        )
                    }

                    Spacer(Modifier.height(NexusSpacing.md))

                    if (conversations.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.ai_chat_history_empty_title),
                            description = stringResource(R.string.ai_chat_history_empty_body),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
                        ) {
                            conversations.forEach { conversation ->
                                ConversationRow(
                                    conversation = conversation,
                                    isCurrent = conversation.id == currentId,
                                    onClick = { onSelect(conversation.id) },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(NexusSpacing.md))
                    NexusOutlineButton(
                        text = stringResource(R.string.ai_chat_new_conversation),
                        onClick = onNew,
                        icon = Icons.Default.Add,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: AIConversation,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val turns = conversation.messages.size
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = NexusShapes.sm,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title.ifBlank { stringResource(R.string.ai_chat_untitled) },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pluralStringResource(R.plurals.ai_chat_message_count, turns, turns),
                        style = NexusTypography.DataLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = " · ${conversation.lastMessageAt.toEpochMilli().toFormattedDate("d MMM")}",
                        style = NexusTypography.DataLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (isCurrent) {
                Text(
                    text = stringResource(R.string.ai_chat_current).uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = ext.signal,
                )
            }
        }
    }
}
