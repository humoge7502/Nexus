package com.nexus.designsystem.effect

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation

/**
 * AutoAnimate's `autoAnimate(parent)` translated to Compose (see
 * `ui-kit/COMPOSE_PORTS.md` Port 2).
 *
 * One modifier replaces every hand-written `Modifier.animateItem()`: when a
 * child is added, removed or reordered the remaining children glide to
 * their new position instead of jumping. The spec lives once in design-
 * system (`NexusAnimation.Springs.itemPlacement` — no-bounce, medium-low
 * stiffness) so feature code never redefines motion.
 *
 * Requirements carried over from AutoAnimate: callers must supply stable,
 * unique `LazyColumn` keys (`key = { ... }`); a reused key animates the
 * wrong element. Removals still use `AnimatedVisibility` with
 * `NexusTransitions.plateExit()` so exits stay quicker than entrances.
 *
 * Under [LocalReducedMotion] the attachment is dropped entirely and items
 * swap instantly — the same off-switch AutoAnimate and Motion apply.
 *
 * This is a [LazyItemScope] extension because `animateItem` only exists on a
 * lazy item; calling it from a plain `Modifier` receiver does not resolve.
 * Call sites inside `items { }` lambdas need no receiver.
 */
@Composable
fun LazyItemScope.nexusItemPlacement(): Modifier =
    if (LocalReducedMotion.current) {
        Modifier
    } else {
        // Placement only: entrances and exits are owned by the screen's
        // AnimatedVisibility/NexusTransitions, so this must not fade as well.
        Modifier.animateItem(placementSpec = NexusAnimation.Springs.itemPlacement)
    }

/**
 * `items(...)` with a mandatory unique [keyOf], so the AutoAnimate rule
 * ("key must be unique or the animation misbehaves") cannot be skipped at
 * the call site. Wrap each item's content root with
 * `Modifier.nexusItemPlacement()` (typically inside a `Box`, as Home,
 * Explore and Applications do) so the placement actually animates.
 */
fun <T> LazyListScope.nexusItems(
    items: List<T>,
    keyOf: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
) {
    items(
        items = items,
        key = keyOf,
    ) { item ->
        itemContent(item)
    }
}