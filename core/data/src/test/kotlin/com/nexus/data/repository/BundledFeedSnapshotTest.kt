package com.nexus.data.repository

import com.google.gson.Gson
import com.nexus.network.dto.FeedResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The bundled asset is what a device that has never reached the network shows, and
 * it is read by the same Gson mapping as the live feed — so a payload the app cannot
 * parse is an empty Explore tab for every fresh install until the next release.
 * This reads the real file, not a fixture, because the file is the product.
 */
class BundledFeedSnapshotTest {

    private val snapshot: FeedResponseDto by lazy {
        val file = File("../../app/src/main/assets/opportunities_seed.json")
        assertTrue("missing bundled snapshot at ${file.path}", file.isFile)
        Gson().fromJson(file.readText(), FeedResponseDto::class.java)
    }

    @Test
    fun `bundled snapshot carries both kinds of opportunity`() {
        assertTrue("no internships seeded", snapshot.internships.isNotEmpty())
        assertTrue("no hackathons seeded", snapshot.hackathons.isNotEmpty())
    }

    @Test
    fun `every internship can be opened and its deadline is readable`() {
        for (row in snapshot.internships) {
            assertTrue("${row.id} has no title or company", row.title.isNotBlank() && row.company.isNotBlank())
            assertTrue("${row.id} apply url is not https: ${row.applyUrl}", row.applyUrl.startsWith("https://"))
            // A source that states no deadline is honest and the app renders it that
            // way; a deadline the app cannot parse silently becomes an unalertable row.
            assertTrue(
                "${row.id} deadline is not an ISO day: ${row.deadline}",
                row.deadline == null || parseCalendarDay(row.deadline) != null,
            )
        }
    }

    @Test
    fun `every hackathon can be opened and dated`() {
        for (row in snapshot.hackathons) {
            assertTrue("${row.id} has no title", row.title.isNotBlank())
            assertTrue("${row.id} url is not https: ${row.registrationUrl}", row.registrationUrl.startsWith("https://"))
            val stated = row.registrationDeadline ?: row.endDate
            assertTrue("${row.id} deadline is not an ISO day: $stated", parseCalendarDay(stated) != null)
        }
    }

    @Test
    fun `no listing is seeded twice`() {
        val ids = snapshot.internships.map { it.id } + snapshot.hackathons.map { it.id }
        assertEquals("duplicate id in the bundled snapshot", ids.size, ids.toSet().size)
    }
}
