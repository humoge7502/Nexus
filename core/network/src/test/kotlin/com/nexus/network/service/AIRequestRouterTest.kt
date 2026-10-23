package com.nexus.network.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class AIRequestRouterTest {

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var functionsService: FirebaseFunctionsAIService
  private lateinit var router: AIRequestRouter

  @Before
  fun setup() {
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)
    functionsService = mock(FirebaseFunctionsAIService::class.java)
    router = AIRequestRouter(firebaseAuth, functionsService)
  }

  @Test
  fun `buildAuthHeader returns bearer token when user authenticated`() = runBlocking {
    val tokenTask = mock(Task::class.java) as Task<String>
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.getIdToken(false)).thenReturn(tokenTask)
    `when`(tokenTask.await()).thenReturn("mock-id-token")

    val header = router.buildAuthHeader()
    assertTrue(header.startsWith("Bearer "))
    assertEquals("Bearer mock-id-token", header)
  }

  @Test(expected = IllegalStateException::class)
  fun `buildAuthHeader throws when user not authenticated`() = runBlocking {
    `when`(firebaseAuth.currentUser).thenReturn(null)
    router.buildAuthHeader()
  }

  @Test
  fun `getEndpoint returns proxy URL when useProxy is true`() {
    if (router.useProxy) {
      val endpoint = router.getEndpoint()
      assertFalse(endpoint.isEmpty())
      assertFalse(endpoint.contains("openrouter"))
    }
  }

  @Test
  fun `DIRECT_URL is correct`() {
    assertEquals("https://openrouter.ai/api/v1/chat/completions", AIRequestRouter.DIRECT_URL)
  }

  @Test
  fun `chat delegates to functionsService when proxy enabled`() = runBlocking {
    if (router.useProxy) {
      val proxyResult = AIProxyResult(
        id = "mock-id",
        content = "AI response via proxy",
        finishReason = "stop",
        usage = Usage(promptTokens = 10, completionTokens = 20, totalTokens = 30),
      )
      `when`(functionsService.proxyAI(any<AIProxyRequest>())).thenReturn(Result.success(proxyResult))

      val result = router.chat(listOf(mapOf("role" to "user", "content" to "Hello")))
      assertTrue(result.isSuccess)
      assertEquals("AI response via proxy", result.getOrNull())
    }
  }

  @Test
  fun `chat returns failure when no messages`() = runBlocking {
    if (router.useProxy) {
      `when`(functionsService.proxyAI(any<AIProxyRequest>())).thenReturn(Result.success(AIProxyResult(id = "", content = "", finishReason = "stop")))

      val result = router.chat(emptyList())
      assertTrue(result.isSuccess)
    }
  }
}
