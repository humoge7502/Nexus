package com.nexus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AIResponseValidatorTest {

    private val validator = AIResponseValidator()

    @Test
    fun `clean career advice raises no warnings`() {
        val result = validator.validate(
            "Build two projects, practice DSA consistently, and apply to roles that match your skills.",
        )

        assertTrue(result.isClean)
        assertTrue(result.warnings.isEmpty())
        assertEquals(WarningSeverity.NONE, result.highestSeverity)
    }

    @Test
    fun `salary claims are flagged as informational`() {
        val result = validator.validate("A backend internship pays around ₹12 LPA at some startups.")

        assertEquals(1, result.warnings.size)
        assertEquals(WarningType.SALARY_CLAIM, result.warnings.first().type)
        assertEquals(WarningSeverity.INFO, result.highestSeverity)
    }

    @Test
    fun `false certainty is flagged at medium severity`() {
        val result = validator.validate("If you follow this plan you are guaranteed an offer.")

        assertEquals(WarningType.FALSE_CERTAINTY, result.warnings.single().type)
        assertEquals(WarningSeverity.MEDIUM, result.highestSeverity)
    }

    @Test
    fun `company generalizations are flagged`() {
        val result = validator.validate("Google always hires candidates with open-source work.")

        assertEquals(WarningType.COMPANY_CLAIM, result.warnings.single().type)
    }

    @Test
    fun `outdated technology advice is flagged`() {
        val result = validator.validate("jQuery is the best choice for every new frontend project.")

        assertEquals(WarningType.OUTDATED_ADVICE, result.warnings.single().type)
        assertEquals(WarningSeverity.MEDIUM, result.highestSeverity)
    }

    @Test
    fun `medical advice escalates to high severity`() {
        val result = validator.validate("Take this dosage before your interview to calm down.")

        assertEquals(WarningType.MEDICAL_ADVICE, result.warnings.single().type)
        assertEquals(WarningSeverity.HIGH, result.highestSeverity)
    }

    @Test
    fun `financial advice is flagged`() {
        val result = validator.validate("You should buy stocks with your first stipend.")

        assertEquals(WarningType.FINANCIAL_ADVICE, result.warnings.single().type)
        assertEquals(WarningSeverity.MEDIUM, result.highestSeverity)
    }

    @Test
    fun `personal identifiers are flagged`() {
        val card = validator.validate("My card number is 1234 5678 9012 3456.")
        assertEquals(WarningType.PERSONAL_INFO, card.warnings.single().type)

        val ssn = validator.validate("It is 123-45-6789.")
        assertEquals(WarningType.PERSONAL_INFO, ssn.warnings.single().type)
    }

    @Test
    fun `harmful advice escalates to high severity`() {
        val result = validator.validate("Just lie on your resume about the internship.")

        assertEquals(WarningType.HARMFUL_ADVICE, result.warnings.single().type)
        assertEquals(WarningSeverity.HIGH, result.highestSeverity)
    }

    @Test
    fun `multiple warnings report the highest severity and stay ordered`() {
        val result = validator.validate(
            "You are guaranteed a job. Google always hires only top candidates. Take this medication dosage daily. Buy stocks now.",
        )

        assertEquals(
            listOf(WarningType.FALSE_CERTAINTY, WarningType.COMPANY_CLAIM, WarningType.MEDICAL_ADVICE, WarningType.FINANCIAL_ADVICE),
            result.warnings.map { it.type },
        )
        assertEquals(WarningSeverity.HIGH, result.highestSeverity)
        assertFalse(result.isClean)
    }

    @Test
    fun `responses are capped at four thousand characters`() {
        val long = "a".repeat(5000)
        val result = validator.validate(long)

        assertEquals(4000, result.content.length)
        assertTrue(result.isClean)
    }
}
