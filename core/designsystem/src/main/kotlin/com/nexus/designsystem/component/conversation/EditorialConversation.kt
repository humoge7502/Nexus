package com.nexus.designsystem.component.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.component.innerLight
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/**
 * The conversation layer.
 *
 * An answer from the mentor is not a chat message — it is a short document the
 * user came to read. So the assistant does not get a bubble: it gets a measure,
 * a baseline, and typography that separates a heading from a list from a code
 * block. Bubbles are for the *user*, whose turn genuinely is a contained object.
 *
 * The byline (a 4dp dot and a mono role label) carries the speaker identity,
 * which frees the container from having to do it.
 */

/** One rendered unit of an assistant answer. */
sealed interface ResponseBlock {
    data class Paragraph(val text: String) : ResponseBlock
    data class Heading(val text: String) : ResponseBlock
    data class BulletList(val items: List<String>) : ResponseBlock
    data class NumberedList(val items: List<String>) : ResponseBlock
    data class Code(val language: String, val body: String) : ResponseBlock
    data class Callout(val label: String, val text: String) : ResponseBlock
}

/** A published source the mentor cited. */
data class SourceReference(
    val index: Int,
    val title: String,
    val domain: String,
    val url: String,
)

/** A next step the mentor is actually proposing. */
data class RecommendedAction(
    val label: String,
    val detail: String = "",
    val onAct: () -> Unit,
)

/**
 * Markdown-ish text from the model, turned into renderable blocks.
 *
 * Deliberately narrow: fenced code, headings, bullets, numbered lists and
 * `> callouts`. Anything else stays a paragraph. A half-implemented CommonMark
 * renderer is worse than none, because it fails silently on the input it did not
 * anticipate.
 */
fun parseAssistantResponse(raw: String): List<ResponseBlock> {
    val lines = raw.replace("\r\n", "\n").split("\n")
    val blocks = mutableListOf<ResponseBlock>()
    var index = 0
    val paragraph = StringBuilder()
    val bullets = mutableListOf<String>()
    val numbered = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotBlank()) blocks += ResponseBlock.Paragraph(paragraph.toString().trim())
        paragraph.setLength(0)
    }

    fun flushBullets() {
        if (bullets.isNotEmpty()) {
            blocks += ResponseBlock.BulletList(bullets.toList())
            bullets.clear()
        }
    }

    fun flushNumbered() {
        if (numbered.isNotEmpty()) {
            blocks += ResponseBlock.NumberedList(numbered.toList())
            numbered.clear()
        }
    }

    fun flushAll() {
        flushParagraph(); flushBullets(); flushNumbered()
    }

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        // Fenced code: consume to the closing fence verbatim, indentation and all.
        if (trimmed.startsWith("```")) {
            flushAll()
            val fence = readCodeFence(lines, index)
            blocks += ResponseBlock.Code(fence.language, fence.body)
            index = fence.nextIndex
            continue
        }

        when {
            trimmed.isEmpty() -> flushAll()

            isHeadingLine(trimmed) -> {
                flushAll()
                blocks += ResponseBlock.Heading(trimmed.trimStart('#').trim())
            }

            isCalloutLine(trimmed) -> {
                flushAll()
                blocks += calloutFrom(trimmed.removePrefix("> ").trim())
            }

            isBulletLine(trimmed) -> {
                flushParagraph(); flushNumbered()
                bullets += bulletText(trimmed)
            }

            isNumberedLine(trimmed) -> {
                flushParagraph(); flushBullets()
                numbered += numberedText(trimmed)
            }

            else -> {
                flushBullets(); flushNumbered()
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(trimmed)
            }
        }
        index++
    }
    flushAll()
    return blocks
}

private val headingPattern = Regex("""^#{1,3} \S.*""")
private val numberedPrefix = Regex("""^\d+[.)]\s+""")

private fun isHeadingLine(line: String) = headingPattern.matches(line)

private fun isCalloutLine(line: String) = line.startsWith("> ")

private fun bulletPrefix(line: String) = line.take(2)

private fun isBulletLine(line: String) =
    bulletPrefix(line) == "- " || bulletPrefix(line) == "* " || bulletPrefix(line) == "• "

private fun bulletText(line: String) = line.drop(2).trim()

private fun isNumberedLine(line: String) = numberedPrefix.containsMatchIn(line)

private fun numberedText(line: String) = line.replace(numberedPrefix, "").trim()

/** `> NOTE: body` splits into a label and a body; a bare `> body` is a NOTE. */
private fun calloutFrom(text: String): ResponseBlock.Callout {
    val colon = text.indexOf(':')
    return if (colon > 0 && colon < 24) {
        ResponseBlock.Callout(text.take(colon).trim(), text.substring(colon + 1).trim())
    } else {
        ResponseBlock.Callout("NOTE", text)
    }
}

/** A fenced block, plus the line index just past its closing fence. */
private class CodeFence(val language: String, val body: String, val nextIndex: Int)

private fun readCodeFence(lines: List<String>, start: Int): CodeFence {
    val language = lines[start].trim().removePrefix("```").trim()
    val body = StringBuilder()
    var index = start + 1
    while (index < lines.size && !lines[index].trim().startsWith("```")) {
        if (body.isNotEmpty()) body.append('\n')
        body.append(lines[index])
        index++
    }
    return CodeFence(language, body.toString(), index + 1)
}

/**
 * Inline `**bold**`, `` `code` `` and `_em_` as styled spans.
 *
 * Rendered rather than stripped because a stray `**` in the middle of career
 * advice is the single clearest tell that text was never really typeset.
 */
@Composable
fun richInline(text: String): AnnotatedString {
    val codeFamily = NexusTypography.Mono
    val codeBackground = MaterialTheme.nexus.hairline

    return remember(text, codeFamily, codeBackground) {
        buildAnnotatedString {
            var cursor = 0
            val pattern = Regex("""(\*\*([^*]+)\*\*|`([^`]+)`|_([^_]+)_)""")
            pattern.findAll(text).forEach { match ->
                if (match.range.first > cursor) append(text.substring(cursor, match.range.first))
                val codeInner = match.value.removeSurrounding("`")
                val boldInner = match.value.removeSurrounding("**")
                val emInner = match.value.removeSurrounding("_")
                when {
                    match.value.startsWith("`") -> {
                        val start = length
                        append(codeInner)
                        addStyle(
                            style = SpanStyle(
                                fontFamily = codeFamily,
                                fontSize = 13.sp,
                                background = codeBackground,
                            ),
                            start = start,
                            end = length,
                        )
                    }

                    match.value.startsWith("**") -> {
                        val start = length
                        append(boldInner)
                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = start,
                            end = length,
                        )
                    }

                    else -> {
                        val start = length
                        append(emInner)
                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.Medium),
                            start = start,
                            end = length,
                        )
                    }
                }
                cursor = match.range.last + 1
            }
            if (cursor < text.length) append(text.substring(cursor))
        }
    }
}

/** The speaker line: a dot that says who, and a mono label that names them. */
@Composable
fun ConversationByline(
    role: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false,
) {
    val ext = MaterialTheme.nexus
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isUser) ext.hairlineStrong else ext.signal),
        )
        Spacer(Modifier.width(NexusSpacing.xs))
        Text(
            text = role.uppercase(),
            style = NexusTypography.DataLabel,
            color = if (isUser) MaterialTheme.colorScheme.onSurfaceVariant else ext.signal,
        )
    }
}

/**
 * Copy, as a labelled text control rather than an icon.
 *
 * It is the only action allowed inside a read surface: nobody retypes a
 * function or a study plan out of a screen, and a label survives a
 * TalkBack pass that a 16dp glyph would not.
 */
@Composable
private fun CopyAffordance(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Text(
        text = "COPY",
        style = NexusTypography.DataLabel,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .clip(NexusShapes.xs)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCopy()
            }
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

/**
 * An assistant answer, typeset rather than boxed.
 *
 * [isStreaming] keeps the trailing caret on the last paragraph only, so a
 * partial answer reads as a sentence being written rather than a blinking
 * cursor parked under finished text.
 */
@Composable
fun AssistantResponse(
    blocks: List<ResponseBlock>,
    modifier: Modifier = Modifier,
    byline: String = "MENTOR",
    isStreaming: Boolean = false,
    onCopy: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ConversationByline(role = byline)
            if (onCopy != null) CopyAffordance(onCopy = onCopy)
        }
        Spacer(Modifier.height(NexusSpacing.sm))

        blocks.forEachIndexed { position, block ->
            val isLast = position == blocks.lastIndex
            when (block) {
                is ResponseBlock.Paragraph -> Text(
                    text = richInline(block.text),
                    style = NexusTypography.EditorialBody,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = NexusSpacing.md),
                )

                is ResponseBlock.Heading -> {
                    Spacer(Modifier.height(NexusSpacing.xs))
                    Text(
                        text = block.text,
                        style = NexusTypography.EditorialTitle,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = NexusSpacing.sm),
                    )
                }

                is ResponseBlock.BulletList -> Column(
                    modifier = Modifier.padding(bottom = NexusSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                ) {
                    block.items.forEach { item ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                text = "—",
                                style = NexusTypography.BodyMedium,
                                color = ext.signal,
                                modifier = Modifier.width(NexusSpacing.lg),
                            )
                            Text(
                                text = richInline(item),
                                style = NexusTypography.EditorialBody,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                is ResponseBlock.NumberedList -> Column(
                    modifier = Modifier.padding(bottom = NexusSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                ) {
                    block.items.forEachIndexed { step, item ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                text = "${step + 1}",
                                style = NexusTypography.Telemetry,
                                color = ext.signal,
                                modifier = Modifier.width(NexusSpacing.lg),
                            )
                            Text(
                                text = richInline(item),
                                style = NexusTypography.EditorialBody,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                is ResponseBlock.Code -> CodeResponseCard(
                    language = block.language,
                    body = block.body,
                    modifier = Modifier.padding(bottom = NexusSpacing.md),
                )

                is ResponseBlock.Callout -> ResponseCallout(
                    label = block.label,
                    text = block.text,
                    modifier = Modifier.padding(bottom = NexusSpacing.md),
                )
            }

            if (isLast && isStreaming) StreamingCaret()
        }
    }
}

/** The user's turn — the one thing in a conversation that is a container. */
@Composable
fun UserMessage(
    text: String,
    modifier: Modifier = Modifier,
    byline: String = "YOU",
) {
    val ext = MaterialTheme.nexus
    val shape = NexusShapes.md

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        ConversationByline(role = byline, isUser = true)
        Spacer(Modifier.height(NexusSpacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(shape)
                .background(ext.signalContainer)
                .border(NexusSpacing.hairline, ext.signal.copy(alpha = 0.5f), shape)
                .innerLight(shape)
                .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
        ) {
            Text(
                text = text,
                style = NexusTypography.BodyMedium,
                color = ext.onSignalContainer,
            )
        }
    }
}

/** A thin block caret that only ever sits at the end of live text. */
@Composable
private fun StreamingCaret() {
    val ext = MaterialTheme.nexus
    val reduced = LocalReducedMotion.current
    val blink = rememberBlink(reduced)

    Box(
        modifier = Modifier
            .width(7.dp)
            .height(16.dp)
            .background(ext.signal.copy(alpha = blink)),
    )
}

@Composable
private fun rememberBlink(reduced: Boolean): Float {
    if (reduced) return 0.85f
    val transition = rememberInfiniteTransition(label = "caret")
    val value by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(560, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "caretAlpha",
    )
    return value
}

/**
 * A code answer on a plate.
 *
 * The language is the plate label, so a block never arrives as an anonymous
 * rectangle, and the copy affordance is a real button because nobody should
 * retype a function out of a chat window.
 */
@Composable
fun CodeResponseCard(
    body: String,
    modifier: Modifier = Modifier,
    language: String = "",
    onCopy: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val shape = NexusShapes.md

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, shape)
            .innerLight(shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = language.ifBlank { "CODE" }.uppercase(),
                style = NexusTypography.DataLabel,
                color = ext.signal,
            )
            if (onCopy != null) CopyAffordance(onCopy = onCopy)
        }
        HorizontalRule(color = ext.hairline)
        Text(
            text = body,
            style = NexusTypography.Telemetry.copy(
                fontFamily = NexusTypography.Mono,
                fontSize = 13.sp,
                lineHeight = 20.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(NexusSpacing.md),
        )
    }
}

/** A cited source: numbered, tappable, and honest about which site it came from. */
@Composable
fun SourceReferenceCard(
    source: SourceReference,
    modifier: Modifier = Modifier,
    onOpen: (String) -> Unit = {},
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.sm

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, shape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpen(source.url)
            }
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = source.index.toString(),
            style = NexusTypography.Telemetry,
            color = ext.structure,
            modifier = Modifier.width(NexusSpacing.lg),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.title,
                style = NexusTypography.TitleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (source.domain.isNotBlank()) {
                Text(
                    text = source.domain.removePrefix("www."),
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
    }
}

/** The step the mentor is proposing, with the control to take it attached. */
@Composable
fun ActionRecommendationCard(
    action: RecommendedAction,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.signal.copy(alpha = 0.35f), shape)
            .innerLight(shape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                action.onAct()
            }
            .padding(NexusSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // A signal tick rather than an icon: it is the same 2dp mark the
        // instrument surfaces use to mean "this one is live".
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 30.dp)
                .background(ext.signal),
        )
        Spacer(Modifier.width(NexusSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.label,
                style = NexusTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (action.detail.isNotBlank()) {
                Spacer(Modifier.height(NexusSpacing.xxxs))
                Text(
                    text = action.detail,
                    style = NexusTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(NexusSpacing.sm))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = null,
            tint = ext.signal,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Reasoning the model exposed, collapsed by default.
 *
 * Collapsed rather than hidden, and labelled rather than decorative: a user
 * who wants to audit the advice can open it, and one who does not is not made
 * to scroll past it.
 */
@Composable
fun ReasoningSummary(
    text: String,
    modifier: Modifier = Modifier,
    label: String = "HOW THIS WAS REASONED",
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    val reduced = LocalReducedMotion.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NexusShapes.xs)
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    expanded = !expanded
                }
                .padding(vertical = NexusSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = if (reduced) fadeIn() else fadeIn(tween(NexusAnimation.Duration.standard)) +
                expandVertically(tween(NexusAnimation.Duration.standard)),
            exit = if (reduced) fadeOut() else fadeOut(tween(NexusAnimation.Duration.fast)) +
                shrinkVertically(tween(NexusAnimation.Duration.fast)),
        ) {
            Text(
                text = richInline(text),
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = NexusSpacing.xs)
                    .clip(NexusShapes.sm)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(NexusSpacing.md),
            )
        }
    }
}

/** A emphasised instruction inside an answer — the "do this first" line. */
@Composable
fun ResponseCallout(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    tone: Color = MaterialTheme.nexus.signal,
) {
    val shape = NexusShapes.sm
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tone.copy(alpha = 0.08f))
            .border(NexusSpacing.hairline, tone.copy(alpha = 0.3f), shape)
            .padding(NexusSpacing.md),
    ) {
        Text(
            text = label.uppercase(),
            style = NexusTypography.DataLabel,
            color = tone,
        )
        Spacer(Modifier.height(NexusSpacing.xs))
        Text(
            text = richInline(text),
            style = NexusTypography.BodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HorizontalRule(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NexusSpacing.hairline)
            .background(color),
    )
}

/** The quiet rail of openers under the composer. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptSuggestionRow(
    prompts: List<String>,
    modifier: Modifier = Modifier,
    label: String = "SUGGESTED",
    enabled: Boolean = true,
    onPrompt: (String) -> Unit,
) {
    val ext = MaterialTheme.nexus
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(NexusSpacing.xs))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(NexusSpacing.hairline)
                    .background(ext.hairline),
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
        ) {
            prompts.forEach { prompt ->
                PromptSuggestion(
                    text = prompt,
                    enabled = enabled,
                    onClick = { onPrompt(prompt) },
                )
            }
        }
    }
}

@Composable
fun PromptSuggestion(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.sm

    Row(
        modifier = modifier
            .pressScale()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                NexusSpacing.hairline,
                if (enabled) ext.hairline else ext.hairline.copy(alpha = 0.4f),
                shape,
            )
            .clickable(enabled = enabled, role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 13.dp)
                .background(if (enabled) ext.signal else ext.hairlineStrong),
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Text(
            text = text,
            style = NexusTypography.BodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            },
        )
    }
}
