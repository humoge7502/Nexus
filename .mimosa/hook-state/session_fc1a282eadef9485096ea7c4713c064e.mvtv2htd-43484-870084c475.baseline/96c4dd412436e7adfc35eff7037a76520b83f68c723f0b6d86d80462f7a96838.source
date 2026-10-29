package com.nexus.designsystem.effect

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.nexus.common.Constants
import kotlinx.coroutines.delay

fun <T> LazyListScope.staggeredItems(
    items: List<T>,
    itemKey: ((T) -> String)? = null,
    staggerDelayMs: Long = 50,
    itemContent: @Composable (T, Int) -> Unit,
) {
    items(count = items.size, key = { index -> itemKey?.invoke(items[index]) ?: index }) { index ->
        val item = items[index]
        val visible = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(index * staggerDelayMs)
            visible.value = true
        }
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(animationSpec = tween(Constants.ANIMATION_DURATION_NORMAL)) +
                slideInVertically(
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                    initialOffsetY = { it / 3 },
                ),
        ) {
            itemContent(item, index)
        }
    }
}
