package com.nexus.designsystem.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import com.nexus.common.Constants
import java.util.concurrent.TimeUnit

@Composable
fun GoalCompletionCelebration(
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isEnabled) return

    val parties = remember {
        listOf(
            Party(
                colors = listOf(
                    0xFF4F8EF7.toInt(),
                    0xFF00D9A3.toInt(),
                    0xFFF72585.toInt(),
                    0xFF7B5EA7.toInt(),
                    0xFFFF6B35.toInt(),
                ),
                emitter = Emitter(duration = Constants.ANIMATION_DURATION_FAST.toLong(), TimeUnit.MILLISECONDS).max(100),
            ),
        )
    }

    KonfettiView(
        parties = parties,
        modifier = modifier,
    )
}
