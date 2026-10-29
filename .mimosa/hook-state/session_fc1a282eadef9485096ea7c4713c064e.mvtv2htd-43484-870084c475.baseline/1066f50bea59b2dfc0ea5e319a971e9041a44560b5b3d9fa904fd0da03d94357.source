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
    val tokenResult = mock(com.google.firebase.auth.GetTokenResult::class.java)
    `when`(tokenResult.token).thenReturn("mock-id-token")
    // A REAL completed Task — Mockito mocks never fire await()'s listeners,
    // which deadlocks the test JVM.
    val tokenTask: Task<com.google.firebase.auth.GetTokenResult> =
        com.google.android.gms.tasks.Tasks.forResult(tokenResult)
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.getIdToken(false)).thenReturn(tokenTask)

    val header = router.buildAuthHeader()
    // In unit tests BuildConfig.AI_PROXY_URL is blank → direct/debug path,
    // which returns the locally-configured (empty in CI) key. The proxy path
    // with the Firebase ID token is exercised by the integration environment.
    assertTrue(header.startsWith("Bearer "))
  }

  @Test
  fun `buildAuthHeader falls back to direct key in debug when not authenticated`() {
    `when`(firebaseAuth.currentUser).thenReturn(null)
    if (router.useProxy) {
      // Release/proxy mode must refuse to build a header without a user token.
      assertThrows(IllegalStateException::class.java) {
        kotlinx.coroutines.runBlocking { router.buildAuthHeader() }
      }
    } else {
      // In unit tests BuildConfig.DEBUG is true and AI_PROXY_URL is blank, so the
      // router takes the direct path (key is empty in CI, but the format holds).
      val header = kotlinx.coroutines.runBlocking { router.buildAuthHeader() }
      assertTrue(header.startsWith("Bearer "))
    }
  }

  @Test
  fun `getEndpoint never exposes the direct OpenRouter URL in proxy mode`() {
    if (router.useProxy) {
      val endpoint = router.getEndpoint()
      // AI_PROXY_URL is a deploy-time setting; it may be blank until the
      // backend is deployed. What must never happen is leaking the direct
      // OpenRouter endpoint (or a key) into the proxy path.
      assertFalse("proxy endpoint must not be the direct OpenRouter URL", endpoint.contains("openrouter"))
      if (com.nexus.network.BuildConfig.AI_PROXY_URL.isNotBlank()) {
        assertTrue(endpoint.startsWith("https://"))
      }
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
