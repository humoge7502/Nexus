package com.nexus.ai

import com.nexus.domain.usecase.AIResponseValidator
import com.nexus.domain.usecase.WarningSeverity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIResponseValidatorTest {
    private lateinit var validator: AIResponseValidator

    @Before
    fun setup() {
        validator = AIResponseValidator()
    }

    @Test
    fun `safe response passes with no warnings`() = runTest {
        val result = validator.validate("Kotlin is a modern programming language used for Android development.")
        assertEquals(com.nexus.domain.usecase.WarningSeverity.NONE, result.highestSeverity)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `response with medical advice gets warning`() = runTest {
        val result = validator.validate("You should take this medication for your condition.")
        assertTrue(result.highestSeverity == com.nexus.domain.usecase.WarningSeverity.HIGH)
    }

    @Test
    fun `response with financial advice gets warning`() = runTest {
        val result = validator.validate("Invest all your money in this stock right now.")
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun `response is trimmed to max length`() = runTest {
        val longText = "A".repeat(5000)
        val result = validator.validate(longText)
        assertTrue(result.content.length <= 4000)
    }

    @Test
    fun `empty response returns empty content`() = runTest {
        val result = validator.validate("")
        assertEquals("", result.content)
    }

    @Test
    fun `response with personal info gets privacy warning`() = runTest {
        val result = validator.validate("My credit card number is 4111-1111-1111-1111 and I live at 123 Main St.")
        assertTrue(result.highestSeverity == com.nexus.domain.usecase.WarningSeverity.MEDIUM)
    }
}
