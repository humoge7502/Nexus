package com.nexus.network.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nexus.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class AuthInterceptorTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var userPreferences: UserPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authInterceptor: AuthInterceptor

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        userPreferences = mock(UserPreferences::class.java)
        firebaseAuth = mock(FirebaseAuth::class.java)

        `when`(userPreferences.authToken)
            .thenReturn(MutableStateFlow("test-token"))
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)

        authInterceptor = AuthInterceptor(userPreferences, firebaseAuth)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `intercept adds auth header when token present`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val request = okhttp3.Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer test-token", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `intercept skips auth header when no token`() = runBlocking {
        `when`(userPreferences.authToken)
            .thenReturn(MutableStateFlow(null))
        authInterceptor.updateCachedToken(null)

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val request = okhttp3.Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals(null, recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `authenticator refreshes token on 401`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authInterceptor)
            .build()

        val request = okhttp3.Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        val response = client.newCall(request).execute()
        assertNotNull(response)
        assertEquals(200, response.code)
    }
}
