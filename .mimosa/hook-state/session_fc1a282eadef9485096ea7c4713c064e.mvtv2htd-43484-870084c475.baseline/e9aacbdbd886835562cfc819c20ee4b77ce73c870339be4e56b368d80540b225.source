package com.nexus.network.service

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AIStreamClientTest {

    private lateinit var server: MockWebServer
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var client: AIStreamClient

    private val messages = listOf(mapOf("role" to "user", "content" to "Hi"))

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        firebaseAuth = mock()
        client = AIStreamClient(firebaseAuth, OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun sseResponse(vararg lines: String): MockResponse = MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody(lines.joinToString("\n\n") + "\n\n")

    private fun collectEvents(): List<AIStreamEvent> = runBlocking {
        client.streamWithToken(
            token = "test-token",
            messages = messages,
            baseUrl = server.url("/streamAI").toString(),
        ).toList()
    }

    @Test
    fun `streams deltas in order and reports final usage`() {
        server.enqueue(
            sseResponse(
                ": OPENROUTER PROCESSING",
                """data: {"choices":[{"delta":{"content":"Hel"}}]}""",
                """data: {"choices":[{"delta":{"content":"lo"}}]}""",
                """data: {"choices":[{"delta":{},"finish_reason":"stop"}]}""",
                """data: {"choices":[],"usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":42}}""",
                "data: [DONE]",
            )
        )

        val events = collectEvents()

        assertEquals(
            listOf(
                AIStreamEvent.Delta("Hel"),
                AIStreamEvent.Delta("lo"),
                AIStreamEvent.Completed(42),
            ),
            events,
        )
    }

    @Test
    fun `posts the bearer token and bounded prompt payload`() {
        server.enqueue(sseResponse("data: [DONE]"))

        collectEvents()

        val recorded = server.takeRequest()
        assertEquals("Bearer test-token", recorded.getHeader("Authorization"))
        assertEquals("text/event-stream", recorded.getHeader("Accept"))
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"messages\""))
        assertTrue(body.contains("\"model\":\"openai/gpt-4o-mini\""))
        assertTrue(body.contains("\"max_tokens\":1024"))
        assertTrue(body.contains("Hi"))
    }

    @Test
    fun `maps quota failures to the server message`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """{"error":{"status":"RESOURCE_EXHAUSTED","message":"Daily AI request limit reached. Try again tomorrow."}}"""
                )
        )

        val failed = collectEvents().single() as AIStreamEvent.Failed

        assertEquals(429, failed.httpStatus)
        assertEquals("Daily AI request limit reached. Try again tomorrow.", failed.message)
    }

    @Test
    fun `falls back to a friendly message for opaque auth failures`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "text/plain")
                .setBody("unauthorized")
        )

        val failed = collectEvents().single() as AIStreamEvent.Failed

        assertEquals(401, failed.httpStatus)
        assertEquals("Sign in again to keep using NEXUS AI.", failed.message)
    }

    @Test
    fun `ignores malformed chunks without losing later deltas`() {
        server.enqueue(
            sseResponse(
                "data: not-json-at-all",
                """data: {"choices":null}""",
                """data: {"choices":[{"delta":{"content":"ok"}}]}""",
                "data: [DONE]",
            )
        )

        val events = collectEvents()

        assertEquals(
            listOf(AIStreamEvent.Delta("ok"), AIStreamEvent.Completed(0)),
            events,
        )
    }

    @Test
    fun `surfaces in-band error events`() {
        server.enqueue(
            sseResponse(
                """data: {"error":{"message":"upstream exploded"}}""",
                """data: {"choices":[{"delta":{"content":"never"}}]}""",
            )
        )

        val failed = collectEvents().single() as AIStreamEvent.Failed

        assertEquals("upstream exploded", failed.message)
    }

    @Test
    fun `stream completes with zero tokens when upstream closes without DONE`() {
        server.enqueue(sseResponse("""data: {"choices":[{"delta":{"content":"a"}}]}"""))

        val events = collectEvents()

        assertEquals(
            listOf(AIStreamEvent.Delta("a"), AIStreamEvent.Completed(0)),
            events,
        )
    }

    @Test
    fun `requires authentication when no user is signed in`() = runBlocking {
        whenever(firebaseAuth.currentUser).thenReturn(null)

        val failed = client.stream(messages).toList().single() as AIStreamEvent.Failed

        assertEquals(401, failed.httpStatus)
        assertTrue(failed.message.contains("Sign in"))
    }

    @Test
    fun `derives the stream URL from the configured proxy URL`() {
        assertEquals(
            "https://us-central1-prod.cloudfunctions.net/streamAI",
            client.streamUrl(
                proxyUrl = "https://us-central1-prod.cloudfunctions.net/proxyAI/",
                projectId = null,
            ),
        )
    }

    @Test
    fun `derives the stream URL from the Firebase project when unconfigured`() {
        assertEquals(
            "https://us-central1-demo-nexus.cloudfunctions.net/streamAI",
            client.streamUrl(proxyUrl = "", projectId = "demo-nexus"),
        )
    }

    @Test
    fun `stream URL requires a project when no proxy URL is set`() {
        var thrown: IllegalArgumentException? = null
        try {
            client.streamUrl(proxyUrl = "", projectId = null)
        } catch (e: IllegalArgumentException) {
            thrown = e
        }
        assertTrue(thrown?.message?.contains("Firebase project") == true)
    }

    @Test
    fun `parses SSE payload variants`() {
        assertEquals(AIStreamClient.StreamChunk.Done, client.parseSseData(" [DONE] "))
        assertEquals(AIStreamClient.StreamChunk.Ignored, client.parseSseData("  "))
        assertEquals(AIStreamClient.StreamChunk.Ignored, client.parseSseData("{oops"))
        assertEquals(
            AIStreamClient.StreamChunk.Delta("hi"),
            client.parseSseData("""{"choices":[{"delta":{"content":"hi"}}]}"""),
        )
        assertEquals(
            AIStreamClient.StreamChunk.Usage(7),
            client.parseSseData("""{"choices":[],"usage":{"total_tokens":7}}"""),
        )
        assertEquals(
            AIStreamClient.StreamChunk.Error("boom"),
            client.parseSseData("""{"error":{"message":"boom"}}"""),
        )
    }
}
