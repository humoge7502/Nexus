package com.nexus.designsystem.effect

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

data class SharedTransitionKey(
    val id: String,
    val type: TransitionType,
    val namespace: String = "default"
) {
    enum class TransitionType { BOUNDS, ELEMENT }
}

@Composable
fun rememberSharedTransitionScope(): Any? {
    return remember { Any() }
}

@Composable
fun SharedElementWrapper(
    key: SharedTransitionKey,
    sharedTransitionScope: Any,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    content: @Composable () -> Unit
) {
    content()
}
