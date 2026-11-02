package com.nexus.domain.usecase

import com.nexus.domain.model.*
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class OpportunityMatchingEngineTest {
    private lateinit var engine: OpportunityMatchingEngine
    private val baseInternship = Internship(
        id = "1", title = "Test", company = Company("TestCo"),
        location = Location("Remote"), stipend = null,
        duration = Duration(3), deadline = LocalDate.now(),
        applyUrl = "https://test.com", postedAt = Instant.now(),
        isRemote = true, isSaved = false,
    )

    @Before
    fun setUp() { engine = OpportunityMatchingEngine() }

    @Test
    fun `empty skills returns base score`() {
        val score = engine.calculateMatchScore(baseInternship, emptyList())
        assertEquals(0.5f, score)
    }

    @Test
    fun `all skills matched returns 1`() {
        val internship = baseInternship.copy(
            requiredSkills = persistentListOf(Skill(name = "Kotlin"), Skill(name = "Android"))
        )
        val score = engine.calculateMatchScore(internship, listOf(Skill(name = "Kotlin"), Skill(name = "Android")))
        assertEquals(1.0f, score)
    }

    @Test
    fun `half skills matched returns 0_5`() {
        val internship = baseInternship.copy(
            requiredSkills = persistentListOf(Skill(name = "Kotlin"), Skill(name = "Python"))
        )
        val score = engine.calculateMatchScore(internship, listOf(Skill(name = "Kotlin")))
        assertEquals(0.5f, score)
    }

    @Test
    fun `a requirement written differently still counts`() {
        val internship = baseInternship.copy(
            requiredSkills = persistentListOf(Skill(name = "React.js"), Skill(name = "Android Development"))
        )
        val score = engine.calculateMatchScore(
            internship,
            listOf(Skill(name = "React"), Skill(name = "Android")),
        )
        assertEquals(1.0f, score)
    }

    @Test
    fun `assess names the requirements in the listing's own words`() {
        val internship = baseInternship.copy(
            requiredSkills = persistentListOf(Skill(name = "JavaScript"), Skill(name = "Java"))
        )
        val fit = engine.assess(internship, listOf(Skill(name = "Python"), Skill(name = "SQL")))
        assertEquals(0f, fit.ratio)
        assertEquals(emptyList<String>(), fit.matched)
        assertEquals(listOf("JavaScript", "Java"), fit.missing)
    }

    @Test
    fun `duplicate requirements are counted once`() {
        val internship = baseInternship.copy(
            requiredSkills = persistentListOf(Skill(name = "Python"), Skill(name = "python"), Skill(name = "Python "))
        )
        val fit = engine.assess(internship, listOf(Skill(name = "Python")))
        assertEquals(1, fit.required)
        assertEquals(1.0f, fit.ratio)
    }

    @Test
    fun `a listing that states nothing is unrankable, not zero`() {
        val fit = engine.assess(baseInternship, listOf(Skill(name = "Kotlin")))
        assertEquals(0, fit.required)
        assertEquals(0f, fit.ratio)
        assertTrue(fit.missing.isEmpty())
    }
}
