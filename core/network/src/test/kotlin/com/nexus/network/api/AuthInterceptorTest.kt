package com.nexus.network.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.nexus.datastore.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever
import java.util.Base64

class AuthInterceptorTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var userPreferences: UserPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tokenProvider: AuthTokenProvider
    private lateinit var authInterceptor: AuthInterceptor

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        userPreferences = mock(UserPreferences::class.java)
        firebaseAuth = mock(FirebaseAuth::class.java)

        `when`(userPreferences.authToken).thenReturn(MutableStateFlow(null))
        tokenProvider = AuthTokenProvider(
            firebaseAuth = firebaseAuth,
            userPreferences = userPreferences,
            scope = CoroutineScope(Dispatchers.Unconfined),
        )
        authInterceptor = AuthInterceptor(tokenProvider)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `intercept adds auth header when token present`() {
        authInterceptor.updateCachedToken(VALID_JWT)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        execute(mockWebServer.url("/").toString())

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer $VALID_JWT", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `intercept skips auth header for third-party hosts`() {
        authInterceptor.updateCachedToken(VALID_JWT)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val response = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .dns(object : okhttp3.Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> =
                    listOf(java.net.InetAddress.getByName("127.0.0.1"))
            })
            .build()
            .newCall(
                okhttp3.Request.Builder()
                    .url("http://example.com:${mockWebServer.port}/test")
                    .build(),
            )
            .execute()
        response.close()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals(null, recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `intercept skips auth header when no token`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        execute("http://localhost:${mockWebServer.port}/test")

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals(null, recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `authenticator refreshes token on 401`() {
        val user = mock(FirebaseUser::class.java)
        val tokenResult = mock(GetTokenResult::class.java)
        `when`(tokenResult.token).thenReturn(VALID_JWT)
        `when`(firebaseAuth.currentUser).thenReturn(user)
        whenever(user.getIdToken(true)).thenReturn(Tasks.forResult(tokenResult))
        whenever(user.getIdToken(false)).thenReturn(Tasks.forResult(tokenResult))

        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authInterceptor)
            .build()

        val request = okhttp3.Request.Builder()
            .url("http://localhost:${mockWebServer.port}/test")
            .build()

        val response = runBlocking { client.newCall(request).execute() }
        assertNotNull(response)
        assertEquals(200, response.code)
        val initial = mockWebServer.takeRequest()
        val retried = mockWebServer.takeRequest()
        assertEquals(null, initial.getHeader("Authorization"))
        assertEquals("Bearer $VALID_JWT", retried.getHeader("Authorization"))
    }

    @Test
    fun `jwt expiry detects expired token`() {
        assertTrue(JwtExpiry.isExpiring(jwtWithExp(nowSeconds() - 10), 300))
    }

    @Test
    fun `jwt expiry accepts token valid for an hour`() {
        assertFalse(JwtExpiry.isExpiring(jwtWithExp(nowSeconds() + 3600), 300))
    }

    @Test
    fun `jwt expiry fails safe on malformed token`() {
        assertFalse(JwtExpiry.isExpiring("not-a-jwt", 300))
    }

    private fun execute(url: String) {
        val response = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
            .newCall(okhttp3.Request.Builder().url(url).build())
            .execute()
        response.close()
    }

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1000

    private fun jwtWithExp(exp: Long): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"none"}""".toByteArray())
        val payload = encoder.encodeToString("""{"exp":$exp}""".toByteArray())
        return "$header.$payload.signature"
    }

    private companion object {
        val VALID_JWT = Base64.getUrlEncoder().withoutPadding().run {
            val expiry = System.currentTimeMillis() / 1000 + 3600
            val header = encodeToString("""{"alg":"none"}""".toByteArray())
            val payload = encodeToString("""{"exp":$expiry}""".toByteArray())
            "$header.$payload.signature"
        }
    }
}
