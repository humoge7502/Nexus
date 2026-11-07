package com.nexus.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nexus.designsystem.theme.nexus

/**
 * Nexus pull-to-refresh shell (Material3 `PullToRefreshBox` + signal
 * indicator — Motion Primitives / reui "empty/loading" behaviour class,
 * **not** a decoration).
 *
 * The indicator is the signal colour on the surface container, so a pull
 * reads as the instrument acknowledging the gesture rather than a
 * generic spinner. Content displacement follows the Material default
 * (content follows the indicator); no custom physics is introduced here
 * because `NexusAnimation` already owns motion and a second spring would
 * fight it.
 *
 * Reduced-motion note: pull-to-refresh stays an available data action (not decoration);
 * the spec lives once in design-system so no second spring fights it.
 *
 * @param isRefreshing whether the screen's load is in flight.
 * @param onRefresh invoked on release past the threshold; typically
 *   `viewModel::refresh` / `viewModel::retryOpportunities`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    val signal = MaterialTheme.nexus.signal

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier,
                color = signal,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
        },
    ) {
        Box(modifier = Modifier) { content() }
    }
}
