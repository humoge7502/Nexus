package com.nexus.domain.usecase

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A saved or pipeline-tracked opportunity whose deadline Sagitta can warn about.
 * Deadlines in the verified feed are calendar days, so every tier below is a day
 * boundary — the app never claims to know an hour the source never published.
 */
data class WatchedDeadline(
    val kind: String,
    val id: String,
    val title: String,
    val org: String,
    val deadline: LocalDate,
)

enum class ReminderTier { FINAL_HOURS, TODAY, TOMORROW, THREE_DAYS, WEEK }

/**
 * Decides which deadline reminder a student should hear about, at most once per
 * opportunity per tier.
 *
 * Each tier owns a disjoint band of days left, so a sweep can only escalate:
 * one warning at a week out, one at three days, one at one day, one on the day
 * itself, one in the final hours. Saving something late simply means fewer of
 * the early bands were ever reachable — the day-of warning still fires.
 */
object DeadlineReminderPlanner {

    /** Local hour after which "closes today" becomes "final hours". */
    const val FINAL_HOURS_HOUR = 18

    /** Most urgent first; the first unfired entry is the one shown. */
    private val byUrgency = listOf(
        ReminderTier.FINAL_HOURS,
        ReminderTier.TODAY,
        ReminderTier.TOMORROW,
        ReminderTier.THREE_DAYS,
        ReminderTier.WEEK,
    )

    data class Firing(val tier: ReminderTier, val key: String)

    fun keyOf(item: WatchedDeadline, tier: ReminderTier): String = "${item.kind}:${item.id}:${tier.name}"

    fun daysLeft(item: WatchedDeadline, today: LocalDate): Long =
        ChronoUnit.DAYS.between(today, item.deadline)

    /**
     * @param firedKeys reminder keys already notified on this device
     * @return the tier to notify plus its dedupe key, or null when nothing is due
     *         or everything due has already fired
     */
    fun decide(
        item: WatchedDeadline,
        today: LocalDate,
        hourOfDay: Int,
        firedKeys: Set<String>,
    ): Firing? {
        val daysLeft = daysLeft(item, today)
        if (daysLeft < 0) return null

        val tier = byUrgency.firstOrNull { candidate ->
            candidate.applies(daysLeft, hourOfDay) && keyOf(item, candidate) !in firedKeys
        } ?: return null

        return Firing(tier = tier, key = keyOf(item, tier))
    }

    private fun ReminderTier.applies(daysLeft: Long, hourOfDay: Int): Boolean = when (this) {
        ReminderTier.FINAL_HOURS -> daysLeft == 0L && hourOfDay >= FINAL_HOURS_HOUR
        ReminderTier.TODAY -> daysLeft == 0L
        ReminderTier.TOMORROW -> daysLeft == 1L
        ReminderTier.THREE_DAYS -> daysLeft in 2L..3L
        ReminderTier.WEEK -> daysLeft in 4L..7L
    }
}
