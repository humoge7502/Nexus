package com.nexus.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Restrained geometry. This product is an instrument, not a toy: corners are
 * cut close, and the smallest label surfaces are nearly square. Pills are
 * reserved for a single status token, never for every chip on the screen.
 */
object NexusShapes {
    /** Near-square instrument tag — the counterweight to rounded cards. */
    val tag = RoundedCornerShape(2.dp)
    val xxs = RoundedCornerShape(4.dp)
    val xs = RoundedCornerShape(6.dp)
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(14.dp)
    val lg = RoundedCornerShape(18.dp)
    val xl = RoundedCornerShape(22.dp)
    val xxl = RoundedCornerShape(26.dp)
    val pill = RoundedCornerShape(50)
    val topSheet = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

    val Material = Shapes(
        extraSmall = xxs,
        small = xs,
        medium = sm,
        large = md,
        extraLarge = xl,
    )
}
