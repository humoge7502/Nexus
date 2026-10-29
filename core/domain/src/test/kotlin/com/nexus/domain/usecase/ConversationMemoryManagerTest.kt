package com.nexus.domain.usecase

import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationMemoryManagerTest {

    private val manager = ConversationMemoryManager()

    private fun message(index: Int, role: AiMessageRole = AiMessageRole.USER) = AIMessage(
        id = "m$index",
        role = role,
        content = "message-$index",
    )

    @Test
    fun `short conversations pass through untouched`() {
        val messages = (1..5).map { message(it) }
        assertEquals(messages, manager.buildContext(messages))
    }

    @Test
    fun `conversations at the limit pass through untouched`() {
        val messages = (1..20).map { message(it) }
        assertEquals(messages, manager.buildContext(messages))
    }

    @Test
    fun `long conversations collapse history into a summary plus recent messages`() {
        val messages = (1..25).map { message(it) }
        val context = manager.buildContext(messages)

        assertEquals(11, context.size)
        assertEquals("summary", context.first().id)
        assertEquals(AiMessageRole.SYSTEM, context.first().role)
        assertEquals(messages.takeLast(10), context.drop(1))
    }

    @Test
    fun `summary counts roles and records topics`() {
        val messages = listOf(
            message(1, AiMessageRole.USER),
            message(2, AiMessageRole.ASSISTANT),
            message(3, AiMessageRole.USER),
        ) + (4..25).map { message(it, AiMessageRole.ASSISTANT) }

        val context = manager.buildContext(messages)
        val summary = context.first().content

        assertTrue(summary.startsWith("Previous conversation: 2 user messages, 13 AI responses."))
        assertTrue(summary.contains("Topics discussed: message-1; message-3."))
    }

    @Test
    fun `summary truncates long topics and keeps at most five`() {
        val longTopic = "x".repeat(120)
        val messages = (1..5).map {
            AIMessage(id = "u$it", role = AiMessageRole.USER, content = "$longTopic-$it")
        } + (6..25).map {
            AIMessage(id = "a$it", role = AiMessageRole.ASSISTANT, content = "reply-$it")
        }

        val summary = manager.buildContext(messages).first().content
        assertTrue(summary.contains("x".repeat(50)))
        assertTrue(summary.contains("Topics discussed:"))
        assertEquals(5, Regex("x{50}").findAll(summary).count())
    }

    @Test
    fun `custom windows are honored`() {
        val messages = (1..10).map { message(it) }
        val context = manager.buildContext(messages, maxMessages = 4, summaryMessageCount = 2)

        assertEquals(3, context.size)
        assertEquals(messages.takeLast(2), context.drop(1))
    }
}
