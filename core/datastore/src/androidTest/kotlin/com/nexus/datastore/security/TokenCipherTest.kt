package com.nexus.datastore.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenCipherTest {

    private val token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEifQ.signature"

    @Test
    fun encryptDecryptRoundTrips() {
        val encrypted = TokenCipher.encrypt(token)

        assertTrue(encrypted.startsWith("enc:v1:"))
        assertNotEquals(token, encrypted)
        assertEquals(token, TokenCipher.decrypt(encrypted))
    }

    @Test
    fun everyEncryptionUsesAFreshIv() {
        val first = TokenCipher.encrypt(token)
        val second = TokenCipher.encrypt(token)

        assertNotEquals(first, second)
        assertEquals(token, TokenCipher.decrypt(first))
        assertEquals(token, TokenCipher.decrypt(second))
    }

    @Test
    fun legacyPlaintextValuesStillDecrypt() {
        assertEquals("legacy-token", TokenCipher.decrypt("legacy-token"))
    }

    @Test
    fun malformedCiphertextFailsClosed() {
        assertNull(TokenCipher.decrypt("enc:v1:"))
        assertNull(TokenCipher.decrypt("enc:v1:not-base64:also-not-base64"))
        assertNull(TokenCipher.decrypt("enc:v1:AAAAAAAAAAAAAAAA:AAAAAAAAAAAAAAAA"))
    }
}
