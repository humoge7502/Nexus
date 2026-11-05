package com.nexus.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResumeAnalysisParserTest {

    private val parser = ResumeAnalysisParser()

    private val report = """
        {"ats_score": 72,
         "sections": [
           {"name": "Contact Info", "score": 95, "note": "Email and phone present."},
           {"name": "Experience", "score": 40, "note": "One internship, no outcomes."}
         ],
         "missing_keywords": ["Kubernetes", "Terraform"],
         "recommendations": ["Quantify the Zoho internship."]}
    """.trimIndent()

    @Test
    fun `reads a well-formed report`() {
        val parsed = parser.parse(report, charactersGiven = 1_200)

        assertEquals(72, parsed?.atsScore)
        assertEquals(2, parsed?.sections?.size)
        assertEquals("Experience", parsed?.sections?.get(1)?.name)
        assertEquals(listOf("Kubernetes", "Terraform"), parsed?.missingKeywords)
        assertEquals(1, parsed?.recommendations?.size)
    }

    @Test
    fun `reads a payload buried in prose and fences`() {
        val noisy = "Here is the analysis:\n```json\n$report\n```\nGood luck!"

        assertEquals(72, parser.parse(noisy, charactersGiven = 1_200)?.atsScore)
    }

    @Test
    fun `reads stringified and fractional scores`() {
        val loose = """{"ats_score":"78.4","sections":[{"name":"Skills","score":"61","note":"ok"}]}"""

        val parsed = parser.parse(loose, charactersGiven = 400)
        assertEquals(78, parsed?.atsScore)
        assertEquals(61, parsed?.sections?.single()?.score)
    }

    @Test
    fun `drops a section it cannot place on the scale but keeps the rest`() {
        val partial = """
            {"ats_score": 60,
             "sections": [
               {"name": "Skills", "score": 80, "note": "Strong."},
               {"name": "Experience", "score": 400, "note": "Nonsense."}
             ]}
        """.trimIndent()

        val parsed = parser.parse(partial, charactersGiven = 400)
        assertEquals(1, parsed?.sections?.size)
        assertEquals("Skills", parsed?.sections?.single()?.name)
    }

    @Test
    fun `refuses a total score off the scale`() {
        assertNull(parser.parse("""{"ats_score":140,"sections":[{"name":"a","score":50}]}""", 400))
    }

    @Test
    fun `refuses a report with no usable sections`() {
        assertNull(parser.parse("""{"ats_score":50,"sections":[]}""", 400))
    }

    @Test
    fun `refuses prose that carries no report`() {
        assertNull(parser.parse("I could not find any sections to score.", 400))
        assertNull(parser.parse("", 400))
    }

    @Test
    fun `counts only the characters the analysis actually saw`() {
        val whole = parser.parse(report, charactersGiven = 900)?.charactersRead
        val prefix = parser.parse(report, charactersGiven = 20_000)?.charactersRead

        assertEquals(900, whole)
        assertEquals(MAX_READABLE_CHARS, prefix)
    }
}
