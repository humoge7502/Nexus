package com.nexus.internships

import com.nexus.domain.model.Company
import com.nexus.domain.model.Duration
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Location
import com.nexus.domain.model.Skill
import com.nexus.domain.model.Stipend
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class InternshipFiltersTest {

    private fun internship(
        id: String,
        title: String,
        company: String = "Acme",
        city: String = "Bengaluru",
        tags: List<String> = emptyList(),
        skills: List<String> = emptyList(),
        remote: Boolean = false,
        stipend: Int? = 50_000,
        description: String = "",
    ) = Internship(
        id = id,
        title = title,
        company = Company(name = company),
        location = Location(city = city),
        stipend = stipend?.let { Stipend(amount = it) },
        duration = Duration(length = 3),
        requiredSkills = skills.map { Skill(name = it) }.toImmutableList(),
        deadline = LocalDate.of(2026, 12, 1),
        applyUrl = "https://example.com/$id",
        description = description,
        isRemote = remote,
        postedAt = Instant.parse("2026-10-01T00:00:00Z"),
        tags = tags.toImmutableList(),
    )

    private val corpus = persistentListOf(
        internship(
            "i1",
            "Android Engineering Intern",
            company = "Uber",
            tags = listOf("Kotlin"),
        ),
        internship(
            "i2",
            "Machine Learning Intern",
            company = "Goldman Sachs",
            skills = listOf("Python", "Machine Learning"),
        ),
        internship(
            "i3",
            "Frontend Intern",
            company = "Zomato",
            tags = listOf("React", "Web"),
            remote = true,
            stipend = null,
        ),
        internship(
            "i4",
            "Cloud Infrastructure Intern",
            company = "AWS",
            skills = listOf("AWS", "Kubernetes"),
            city = "Hyderabad",
        ),
    )

    @Test
    fun `blank query and no filters keeps every internship`() {
        assertEquals(corpus.size, corpus.filterInternships("", emptySet()).size)
    }

    @Test
    fun `search matches title company city and skills case-insensitively`() {
        assertEquals(listOf("i2"), corpus.filterInternships("machine", emptySet()).map { it.id })
        assertEquals(listOf("i1"), corpus.filterInternships("UBER", emptySet()).map { it.id })
        assertEquals(listOf("i4"), corpus.filterInternships("hyderabad", emptySet()).map { it.id })
        assertEquals(listOf("i4"), corpus.filterInternships("kubernetes", emptySet()).map { it.id })
    }

    @Test
    fun `search matches at word boundaries so uber does not hit kubernetes`() {
        assertEquals(listOf("i1"), corpus.filterInternships("uber", emptySet()).map { it.id })
    }

    @Test
    fun `remote filter keeps only remote roles`() {
        assertEquals(listOf("i3"), corpus.filterInternships("", setOf("Remote")).map { it.id })
    }

    @Test
    fun `paid filter drops roles without a stipend`() {
        assertTrue(corpus.filterInternships("", setOf("Paid")).none { it.id == "i3" })
    }

    @Test
    fun `facet filters match tags and skills`() {
        assertEquals(listOf("i2"), corpus.filterInternships("", setOf("AI/ML")).map { it.id })
        assertEquals(listOf("i3"), corpus.filterInternships("", setOf("Web")).map { it.id })
        assertEquals(listOf("i1"), corpus.filterInternships("", setOf("Mobile")).map { it.id })
        assertEquals(listOf("i4"), corpus.filterInternships("", setOf("Cloud")).map { it.id })
    }

    @Test
    fun `filters combine with AND`() {
        assertEquals(
            emptyList<String>(),
            corpus.filterInternships("", setOf("Remote", "Paid")).map { it.id },
        )
        assertEquals(
            listOf("i4"),
            corpus.filterInternships("cloud", setOf("Paid", "Cloud")).map { it.id },
        )
    }
}
