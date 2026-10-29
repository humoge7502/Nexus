package com.nexus.data.repository

import com.nexus.database.entity.InternshipEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * The internships table legitimately holds two deadline encodings: bundled
 * seed assets and backend payloads use epoch millis, while older cached rows
 * and the notification DAO use epoch days. Mapping code must accept both —
 * passing a millisecond value to [LocalDate.ofEpochDay] throws
 * DateTimeException and crashes every collector of the internships flow.
 */
internal fun InternshipEntity.deadlineDate(): LocalDate = deadline.toDeadlineDate()

internal fun Long.toDeadlineDate(): LocalDate =
    if (this > EPOCH_MILLIS_THRESHOLD) {
        Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
    } else {
        LocalDate.ofEpochDay(this)
    }

// 10_000_000_000 epoch days is year 27,378,000 — no real deadline reaches it.
private const val EPOCH_MILLIS_THRESHOLD = 10_000_000_000L
