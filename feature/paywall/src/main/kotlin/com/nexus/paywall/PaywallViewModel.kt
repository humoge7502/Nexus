package com.nexus.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.data.usecase.BillingManager
import com.nexus.domain.model.BillingState
import com.nexus.domain.model.ProductTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = true,
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
    val lifetimePrice: String = "",
    val isPro: Boolean = false,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "paywall"))
        observeBillingState()
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.billingState.collect { state ->
                when (state) {
                    is BillingState.Loaded -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        monthlyPrice = state.products.find { it.tier == ProductTier.PRO_MONTHLY }?.price ?: "",
                        yearlyPrice = state.products.find { it.tier == ProductTier.PRO_YEARLY }?.price ?: "",
                        lifetimePrice = state.products.find { it.tier == ProductTier.LIFETIME }?.price ?: "",
                    )
                    is BillingState.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is BillingState.Unavailable -> _uiState.value = _uiState.value.copy(isLoading = false)
                    is BillingState.Purchased -> _uiState.value = _uiState.value.copy(isPro = true)
                    is BillingState.Error -> _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
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
}
