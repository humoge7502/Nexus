package com.nexus.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * A 4dp grid. Density is the point: an instrument shows more per screen than a
 * marketing page, so gutters stay at 16dp while vertical rhythm tightens.
 */
object NexusSpacing {
    val hairline = 1.dp
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val huge = 48.dp
    val massive = 64.dp

    /** Standard screen gutter. */
    val gutter = 16.dp

    /** Edge inset on immersive screens. */
    val screenEdge = 20.dp

    /** Minimum interactive box; anything smaller is a bug. */
    val touchTarget = 48.dp
    val minTouchTarget = 44.dp
}

object NexusElevation {
    val none = 0.dp
    val xs = 2.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
}
