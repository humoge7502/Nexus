package com.nexus.explore

import com.nexus.domain.model.CertProvider
import com.nexus.domain.model.Certification
import com.nexus.domain.model.Company
import com.nexus.domain.model.DifficultyLevel
import com.nexus.domain.model.Duration
import com.nexus.domain.model.EventMode
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Location
import com.nexus.domain.model.Price
import com.nexus.domain.model.Skill
import com.nexus.domain.model.SkillCategory
import com.nexus.domain.model.Stipend
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ExploreFiltersTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 26)

    private fun internship(
        id: String,
        title: String,
        company: String = "Acme",
        tags: List<String> = emptyList(),
        description: String = "",
        remote: Boolean = false,
        city: String = "Bengaluru",
        stipend: Int? = 50000,
    ) = Internship(
        id = id,
        title = title,
        company = Company(name = company),
        location = Location(city = city),
        stipend = stipend?.let { Stipend(amount = it) },
        duration = Duration(length = 3),
        deadline = today.plusDays(20),
        applyUrl = "https://example.com/$id",
        description = description,
        isRemote = remote,
        postedAt = Instant.parse("2026-10-01T00:00:00Z"),
        tags = tags.toImmutableList(),
    )

    private fun hackathon(
        id: String,
        title: String,
        organizer: String = "Org",
        tags: List<String> = emptyList(),
        description: String = "",
        mode: EventMode = EventMode.ONLINE,
        prize: String = "₹1,00,000",
    ) = Hackathon(
        id = id,
        title = title,
        organizer = organizer,
        description = description,
        mode = mode,
        startDate = today.plusDays(10),
        endDate = today.plusDays(12),
        registrationDeadline = today.plusDays(8),
        prizePool = prize,
        tags = tags.toImmutableList(),
    )

    private fun certification(
        id: String,
        title: String,
        provider: CertProvider = CertProvider.GOOGLE,
        skills: List<String> = emptyList(),
        price: Price = Price.Free,
        category: SkillCategory = SkillCategory.AI_ML,
    ) = Certification(
        id = id,
        title = title,
        provider = provider,
        category = category,
        difficulty = DifficultyLevel.INTERMEDIATE,
        estimatedHours = 40,
        skills = skills.map { Skill(name = it) }.toImmutableList(),
        price = price,
    )

    private val internships = listOf(
        internship("i1", "Android Engineering Intern", company = "Uber", tags = listOf("Kotlin", "Mobile")),
        internship("i2", "Machine Learning Intern", company = "Goldman Sachs", tags = listOf("Python", "AI/ML")),
        internship("i3", "Frontend Intern", company = "Zomato", tags = listOf("React", "Web"), remote = true, stipend = null),
        internship(
            "i4",
            "Sustainability Analyst Intern",
            company = "GreenCo",
            description = "Work on sustainability reporting",
            stipend = 0,
        ),
    )

    private val hackathons = listOf(
        hackathon("h1", "AI Sprint", organizer = "OpenAI", tags = listOf("AI", "ML")),
        hackathon("h2", "Web3 Buildathon", organizer = "Polygon", tags = listOf("Blockchain")),
        hackathon("h3", "Campus Robotics Challenge", organizer = "IIT", mode = EventMode.OFFLINE, prize = "TBD"),
    )

    private val certifications = listOf(
        certification("c1", "Google Data Analytics", provider = CertProvider.COURSERA, skills = listOf("SQL", "Data Science")),
        certification(
            "c2",
            "AWS Cloud Practitioner",
            provider = CertProvider.AWS,
            skills = listOf("Cloud"),
            price = Price.Paid(100),
            category = SkillCategory.CLOUD_COMPUTING,
        ),
    )

    @Test
    fun `default internships category keeps every internship and cross-sells hackathons`() {
        val filtered = internships.filterInternshipsForExplore("", emptySet(), CATEGORY_INTERNSHIPS)
        val filteredHackathons = hackathons.filterHackathonsForExplore("", emptySet(), CATEGORY_INTERNSHIPS)

        assertEquals(4, filtered.size)
        assertEquals(3, filteredHackathons.size)
    }

    @Test
    fun `hackathons category excludes internships and certifications`() {
        assertTrue(internships.filterInternshipsForExplore("", emptySet(), CATEGORY_HACKATHONS).isEmpty())
        assertTrue(certifications.filterCertificationsForExplore("", emptySet(), CATEGORY_HACKATHONS).isEmpty())
        assertEquals(3, hackathons.filterHackathonsForExplore("", emptySet(), CATEGORY_HACKATHONS).size)
    }

    @Test
    fun `certifications category only returns certifications`() {
        assertTrue(internships.filterInternshipsForExplore("", emptySet(), CATEGORY_CERTIFICATIONS).isEmpty())
        assertTrue(hackathons.filterHackathonsForExplore("", emptySet(), CATEGORY_CERTIFICATIONS).isEmpty())
        assertEquals(2, certifications.filterCertificationsForExplore("", emptySet(), CATEGORY_CERTIFICATIONS).size)
    }

    @Test
    fun `search matches title company tags and description case-insensitively`() {
        assertEquals(listOf("i2"), internships.filterInternshipsForExplore("goldman", emptySet(), CATEGORY_INTERNSHIPS).map { it.id })
        assertEquals(listOf("i3"), internships.filterInternshipsForExplore("react", emptySet(), CATEGORY_INTERNSHIPS).map { it.id })
        assertEquals(listOf("i4"), internships.filterInternshipsForExplore("REPORTING", emptySet(), CATEGORY_INTERNSHIPS).map { it.id })
    }

    @Test
    fun `remote and paid filters combine`() {
        assertEquals(listOf("i3"), internships.filterInternshipsForExplore("", setOf("remote"), CATEGORY_INTERNSHIPS).map { it.id })
        assertEquals(
            listOf("i1", "i2"),
            internships.filterInternshipsForExplore("", setOf("paid"), CATEGORY_INTERNSHIPS).map { it.id },
        )
        assertTrue(internships.filterInternshipsForExplore("", setOf("remote", "paid"), CATEGORY_INTERNSHIPS).isEmpty())
    }

    @Test
    fun `skill filters match related tags without matching substrings such as sustainability`() {
        assertEquals(listOf("i2"), internships.filterInternshipsForExplore("", setOf("ai/ml"), CATEGORY_INTERNSHIPS).map { it.id })
        assertEquals(listOf("i1"), internships.filterInternshipsForExplore("", setOf("mobile"), CATEGORY_INTERNSHIPS).map { it.id })
        assertEquals(listOf("i3"), internships.filterInternshipsForExplore("", setOf("web"), CATEGORY_INTERNSHIPS).map { it.id })
    }

    @Test
    fun `research category matches descriptions and offers no hackathons`() {
        val researchInternships = internships + internship(
            "i5",
            "Research Assistant",
            description = "Work in the computer science laboratory",
        )

        assertEquals(
            listOf("i5"),
            researchInternships.filterInternshipsForExplore("", emptySet(), CATEGORY_RESEARCH).map { it.id },
        )
        assertTrue(hackathons.filterHackathonsForExplore("", emptySet(), CATEGORY_RESEARCH).isEmpty())
    }

    @Test
    fun `more category returns all three opportunity types`() {
        assertEquals(4, internships.filterInternshipsForExplore("", emptySet(), CATEGORY_MORE).size)
        assertEquals(3, hackathons.filterHackathonsForExplore("", emptySet(), CATEGORY_MORE).size)
        assertEquals(2, certifications.filterCertificationsForExplore("", emptySet(), CATEGORY_MORE).size)
    }

    @Test
    fun `hackathon search covers organizer and paid filter requires a numeric prize`() {
        assertEquals(listOf("h2"), hackathons.filterHackathonsForExplore("polygon", emptySet(), CATEGORY_HACKATHONS).map { it.id })
        assertEquals(
            listOf("h1", "h2"),
            hackathons.filterHackathonsForExplore("", setOf("paid"), CATEGORY_HACKATHONS).map { it.id },
        )
    }

    @Test
    fun `certification filter matches provider skills and paid price`() {
        assertEquals(listOf("c2"), certifications.filterCertificationsForExplore("aws", emptySet(), CATEGORY_CERTIFICATIONS).map { it.id })
        assertEquals(listOf("c1"), certifications.filterCertificationsForExplore("", setOf("ai/ml"), CATEGORY_CERTIFICATIONS).map { it.id })
        assertEquals(
            listOf("c2"),
            certifications.filterCertificationsForExplore("", setOf("paid"), CATEGORY_CERTIFICATIONS).map { it.id },
        )
    }
}
