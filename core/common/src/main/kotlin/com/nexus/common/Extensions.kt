package com.nexus.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.toLocalDate(): LocalDate =
    LocalDate.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return LocalDate.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault()).format(formatter)
}

fun LocalDate.toEpochMillis(): Long = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun String?.orEmpty(): String = this ?: ""

fun Int.formatWithSuffix(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

fun Float.percentage(): String = "${(this * 100).toInt()}%"
