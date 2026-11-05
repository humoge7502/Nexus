package com.nexus.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.designsystem.component.feedback.EditorialStatusBand
import com.nexus.designsystem.component.feedback.StatusTone
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onPurchased: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    if (uiState.isPro) {
        onPurchased()
        return
    }

    val features = listOf(
        stringResource(R.string.paywall_feature_unlimited_ai_chat),
        stringResource(R.string.paywall_feature_priority_tracking),
        stringResource(R.string.paywall_feature_advanced_insights),
        stringResource(R.string.paywall_feature_resume_optimization),
        stringResource(R.string.paywall_feature_skill_gap),
        stringResource(R.string.paywall_feature_ad_free),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface),
    ) {
        PaywallHero(onDismiss = onDismiss)

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            SectionHeader(
                title = stringResource(R.string.text_pro_features),
                index = "01",
                meta = features.size.toString().padStart(2, '0'),
            )
            Spacer(modifier = Modifier.height(8.dp))
            features.forEachIndexed { index, feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = (index + 1).toString().padStart(2, '0'),
                        style = NexusTypography.TelemetryMicro,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier.width(22.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        // Inclusion is stated by the row itself; a second
                        // attention hue next to the lime header reads as noise.
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(15.dp),
                    )
                }
                if (index != features.lastIndex) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            SectionHeader(
                title = stringResource(R.string.paywall_choose_plan),
                index = "02",
            )
            Spacer(modifier = Modifier.height(14.dp))

            val fault = uiState.billingFault
            if (uiState.isLoading) {
                EditorialSkeletonList(
                    count = 3,
                    loadingLabel = stringResource(R.string.paywall_loading_prices),
                )
            } else if (!uiState.pricesReady) {
                // No Play catalogue means no purchasable plan. Showing the old
                // indicative numbers here would quote a price we cannot charge.
                EditorialErrorState(
                    headline = stringResource(R.string.paywall_prices_unavailable),
                    reason = stringResource(R.string.paywall_prices_reason),
                    actionLabel = stringResource(R.string.paywall_try_again),
                    onAction = viewModel::retryBilling,
                    secondaryHint = uiState.billingFault,
                )
            } else {
                if (fault != null) {
                    EditorialStatusBand(
                        label = stringResource(R.string.paywall_purchase_not_completed),
                        detail = fault,
                        tone = StatusTone.Fault,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // The season pass leads because the simulated cohort's
                    // rejection was about the *commitment window*, not the
                    // amount — a pass that ends with placement season is the
                    // framing they said they could actually accept.
                    PlanCard(
                        index = "01",
                        price = uiState.seasonPrice.ifBlank { PLACEHOLDER_PRICE },
                        enabled = uiState.seasonPrice.isNotBlank(),
                        badge = stringResource(R.string.paywall_season_badge),
                        caption = stringResource(R.string.paywall_season_caption),
                        actionLabel = stringResource(R.string.paywall_subscribe_season),
                        onAction = { viewModel.purchaseSeason(activity) },
                        featured = true,
                    )
                    PlanCard(
                        index = "02",
                        price = uiState.yearlyPrice.ifBlank { PLACEHOLDER_PRICE },
                        enabled = uiState.yearlyPrice.isNotBlank(),
                        // Derived from the two Play prices, and absent when the
                        // arithmetic doesn't hold — a fixed "Save 58%" would be
                        // the one badge a student can check by hand.
                        badge = uiState.yearlySavingPercent?.let {
                            stringResource(R.string.paywall_save_percent, it)
                        },
                        actionLabel = stringResource(R.string.paywall_subscribe_yearly),
                        onAction = { viewModel.purchaseYearly(activity) },
                    )
                    PlanCard(
                        index = "03",
                        price = uiState.monthlyPrice.ifBlank { PLACEHOLDER_PRICE },
                        enabled = uiState.monthlyPrice.isNotBlank(),
                        actionLabel = stringResource(R.string.paywall_subscribe_monthly),
                        onAction = { viewModel.purchaseMonthly(activity) },
                    )
                    PlanCard(
                        index = "04",
                        price = uiState.lifetimePrice.ifBlank { PLACEHOLDER_PRICE },
                        enabled = uiState.lifetimePrice.isNotBlank(),
                        caption = stringResource(R.string.paywall_one_time),
                        actionLabel = stringResource(R.string.paywall_lifetime_access),
                        onAction = { viewModel.purchaseLifetime(activity) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = viewModel::restorePurchases,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.paywall_restore_purchases))
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.paywall_maybe_later),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PaywallHero(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(NexusGradients.PrimaryDeep))
            .instrumentGrid(spacingDp = 28f, alpha = 0.06f, color = NexusColors.SignalInk)
            .sheen(alpha = 0.06f)
            .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 30.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = NexusColors.SignalInk,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.paywall_pro_label),
                        style = NexusTypography.DataLabel,
                        color = NexusColors.SignalInk.copy(alpha = 0.86f),
                    )
                }
                NexusIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = stringResource(R.string.paywall_maybe_later),
                    onClick = onDismiss,
                    tint = NexusColors.SignalInk.copy(alpha = 0.86f),
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.text_unlock_nexus_pro),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = NexusColors.SignalInk,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.text_supercharge_your_career_journey_with_premium),
                style = MaterialTheme.typography.bodyMedium,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun PlanCard(
    index: String,
    price: String,
    actionLabel: String,
    onAction: () -> Unit,
    featured: Boolean = false,
    badge: String? = null,
    caption: String? = null,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(22.dp)
    val borderColor = if (featured) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .hairline(color = borderColor, cornerRadiusDp = 22f)
            .padding(18.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = index,
                    style = NexusTypography.TelemetryMicro,
                    color = if (featured) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = price,
                        style = NexusTypography.Stat.copy(fontSize = 26.sp, lineHeight = 30.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (caption != null) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (badge != null && enabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!enabled) {
                // A plan Play did not price is not for sale this session; say so
                // instead of offering a button that cannot complete.
                Text(
                    text = stringResource(R.string.paywall_plan_unavailable),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (featured) {
                NexusButton(
                    text = actionLabel,
                    onClick = onAction,
                    gradient = NexusGradients.PrimaryDeep,
                )
            } else {
                NexusOutlineButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Rendered in place of a price Play has not supplied — never a made-up figure. */
private const val PLACEHOLDER_PRICE = "—"
