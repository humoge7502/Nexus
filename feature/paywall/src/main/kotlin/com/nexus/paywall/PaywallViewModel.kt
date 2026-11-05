package com.nexus.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.data.usecase.BillingManager
import com.nexus.domain.model.BillingState
import com.nexus.domain.model.ProductTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val MONTHS_PER_YEAR = 12

data class PaywallUiState(
    val isLoading: Boolean = true,
    val seasonPrice: String = "",
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
    val lifetimePrice: String = "",
    val monthlyMicros: Long = 0L,
    val yearlyMicros: Long = 0L,
    val isPro: Boolean = false,
    /** Null only while Play Billing has answered with a usable price list. */
    val billingFault: String? = null,
) {
    /** True when Play returned at least one real price. A plan without one is
     *  shown as unavailable rather than given an invented figure. */
    val pricesReady: Boolean
        get() = seasonPrice.isNotBlank() || monthlyPrice.isNotBlank() ||
            yearlyPrice.isNotBlank() || lifetimePrice.isNotBlank()

    /** Null unless Play priced both plans and yearly genuinely beats twelve
     *  months. A hardcoded discount reads as a real one, and the price card
     *  is the one place a student checks the arithmetic. */
    val yearlySavingPercent: Int?
        get() = twelveMonthlyMicros?.let { twelve ->
            if (yearlyMicros in 1 until twelve) {
                // Floored on the saving itself, so the badge can only ever
                // understate the discount rather than round it up.
                ((twelve - yearlyMicros) * 100 / twelve).toInt()
            } else {
                null
            }
        }

    private val twelveMonthlyMicros: Long?
        get() = monthlyMicros.takeIf { it > 0L }?.let { it * MONTHS_PER_YEAR }
}

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val analyticsTracker: AnalyticsTracker,
    private val userPreferences: com.nexus.datastore.UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "paywall"))
        observeBillingState()
        startBilling()
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.billingState.collect { state ->
                when (state) {
                    is BillingState.Loaded -> {
                        val byTier = state.products.associateBy { it.tier }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            billingFault = null,
                            seasonPrice = byTier[ProductTier.PRO_SEASON]?.price.orEmpty(),
                            monthlyPrice = byTier[ProductTier.PRO_MONTHLY]?.price.orEmpty(),
                            yearlyPrice = byTier[ProductTier.PRO_YEARLY]?.price.orEmpty(),
                            lifetimePrice = byTier[ProductTier.LIFETIME]?.price.orEmpty(),
                            monthlyMicros = byTier[ProductTier.PRO_MONTHLY]?.priceMicros ?: 0L,
                            yearlyMicros = byTier[ProductTier.PRO_YEARLY]?.priceMicros ?: 0L,
                        )
                    }
                    is BillingState.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is BillingState.Unavailable -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        billingFault = "Play Billing isn't reachable on this device.",
                    )
                    is BillingState.Purchased -> {
                        _uiState.value = _uiState.value.copy(isPro = true)
                        userPreferences.saveIsPro(true)
                    }
                    is BillingState.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        billingFault = state.message,
                    )
                }
            }
        }
    }

    private fun startBilling() {
        viewModelScope.launch {
            // Billing may never answer on devices without Play; the screen must still
            // resolve so the user is not left staring at a spinner — but it resolves
            // to "prices unavailable", never to a figure we invented.
            val answered = withTimeoutOrNull(BILLING_TIMEOUT_MS) {
                try {
                    billingManager.initialize()
                    true
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    false
                }
            }
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    billingFault = when {
                        state.pricesReady || state.isPro || state.billingFault != null -> state.billingFault
                        answered == true -> "Play Billing returned no prices."
                        answered == null -> "Play Billing didn't answer in time."
                        else -> "Play Billing could not start."
                    },
                )
            }
        }
    }

    fun retryBilling() {
        _uiState.update { it.copy(isLoading = true, billingFault = null) }
        startBilling()
    }

    fun purchaseSeason(activity: Activity) {
        viewModelScope.launch {
            analyticsTracker.track("paywall_purchase", mapOf("tier" to "season"))
            billingManager.purchase(ProductTier.PRO_SEASON, activity)
        }
    }

    fun purchaseMonthly(activity: Activity) {
        viewModelScope.launch {
            analyticsTracker.track("paywall_purchase", mapOf("tier" to "monthly"))
            billingManager.purchase(ProductTier.PRO_MONTHLY, activity)
        }
    }

    fun purchaseYearly(activity: Activity) {
        viewModelScope.launch {
            analyticsTracker.track("paywall_purchase", mapOf("tier" to "yearly"))
            billingManager.purchase(ProductTier.PRO_YEARLY, activity)
        }
    }

    fun purchaseLifetime(activity: Activity) {
        viewModelScope.launch {
            analyticsTracker.track("paywall_purchase", mapOf("tier" to "lifetime"))
            billingManager.purchase(ProductTier.LIFETIME, activity)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            analyticsTracker.track("paywall_restore")
            billingManager.initialize()
        }
    }

    private companion object {
        const val BILLING_TIMEOUT_MS = 6_000L
    }
}
