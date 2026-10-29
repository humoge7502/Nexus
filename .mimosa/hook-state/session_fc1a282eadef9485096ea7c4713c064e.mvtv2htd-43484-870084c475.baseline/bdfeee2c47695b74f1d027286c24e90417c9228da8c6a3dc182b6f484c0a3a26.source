package com.nexus.domain.usecase

import kotlin.math.roundToInt

/**
 * Percentile of [rank] among [total] participants (1 means the top entrant).
 * Returns 0 when the user is unranked or the board is empty.
 */
fun topPercentOf(rank: Int, total: Int): Int {
    if (rank <= 0 || total <= 0) return 0
    return ((rank.toFloat() / total) * 100f).roundToInt().coerceIn(1, 100)
}
