package com.nexus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DeadlineReminderPlannerTest {

    private val today = LocalDate.of(2026, 11, 1)

    private fun item(closesInDays: Long) = WatchedDeadline(
        kind = "INTERNSHIP",
        id = "opp-1",
        title = "Backend Intern",
        org = "Acme",
        deadline = today.plusDays(closesInDays),
    )

    @Test
    fun `nothing is due while the deadline is further out than a week`() {
        assertNull(DeadlineReminderPlanner.decide(item(8), today, 12, emptySet()))
    }

    @Test
    fun `a past deadline never fires`() {
        assertNull(DeadlineReminderPlanner.decide(item(-1), today, 12, emptySet()))
    }

    @Test
    fun `one week band fires the week tier with the real day count`() {
        val firing = DeadlineReminderPlanner.decide(item(7), today, 9, emptySet())
        assertEquals(ReminderTier.WEEK, firing?.tier)
        assertEquals("INTERNSHIP:opp-1:WEEK", firing?.key)
    }

    @Test
    fun `each band owns a disjoint range of days`() {
        val expected = mapOf(
            7L to ReminderTier.WEEK,
            4L to ReminderTier.WEEK,
            3L to ReminderTier.THREE_DAYS,
            2L to ReminderTier.THREE_DAYS,
            1L to ReminderTier.TOMORROW,
        )
        expected.forEach { (daysLeft, tier) ->
            val firing = DeadlineReminderPlanner.decide(item(daysLeft), today, 12, emptySet())
            assertEquals(tier, firing?.tier)
        }
    }

    @Test
    fun `a tier is never repeated once it has fired`() {
        val fired = setOf("INTERNSHIP:opp-1:WEEK")
        assertNull(DeadlineReminderPlanner.decide(item(6), today, 12, fired))
    }

    @Test
    fun `saving an opportunity late still reaches the day-of warning`() {
        var fired = emptySet<String>()

        val savedLate = item(2)
        val threeDays = DeadlineReminderPlanner.decide(savedLate, today, 12, fired)!!
        assertEquals(ReminderTier.THREE_DAYS, threeDays.tier)
        fired = fired + threeDays.key

        val tomorrow = DeadlineReminderPlanner.decide(item(1), today, 12, fired)!!
        assertEquals(ReminderTier.TOMORROW, tomorrow.tier)
        fired = fired + tomorrow.key

        val dayOf = DeadlineReminderPlanner.decide(item(0), today, 8, fired)!!
        assertEquals(ReminderTier.TODAY, dayOf.tier)
        fired = fired + dayOf.key

        val finalHours = DeadlineReminderPlanner.decide(item(0), today, 19, fired)!!
        assertEquals(ReminderTier.FINAL_HOURS, finalHours.tier)
    }

    @Test
    fun `final hours waits until the evening and the day tier comes first`() {
        val morning = DeadlineReminderPlanner.decide(item(0), today, 7, emptySet())
        assertEquals(ReminderTier.TODAY, morning?.tier)

        val fired = setOf(morning!!.key)
        assertNull(DeadlineReminderPlanner.decide(item(0), today, 15, fired))

        val evening = DeadlineReminderPlanner.decide(item(0), today, DeadlineReminderPlanner.FINAL_HOURS_HOUR, fired)
        assertEquals(ReminderTier.FINAL_HOURS, evening?.tier)
    }

    @Test
    fun `nothing is left to fire once the whole ladder is spent`() {
        val fired = ReminderTier.entries.map { DeadlineReminderPlanner.keyOf(item(0), it) }.toSet()
        assertNull(DeadlineReminderPlanner.decide(item(0), today, 23, fired))
    }

    @Test
    fun `days left is measured against the calendar deadline`() {
        assertEquals(3L, DeadlineReminderPlanner.daysLeft(item(3), today))
        assertEquals(-2L, DeadlineReminderPlanner.daysLeft(item(-2), today))
    }
}
