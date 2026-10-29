package com.nexus.domain.model

enum class ProductTier {
    FREE, PRO_MONTHLY, PRO_YEARLY, LIFETIME
}

data class ProductListing(
    val sku: String,
    val tier: ProductTier,
    val title: String,
    val description: String,
    val price: String,
    val priceMicros: Long,
    val currencyCode: String,
)

data class PurchaseResult(
    val sku: String,
    val purchaseToken: String,
    val isAcknowledged: Boolean,
)

sealed interface BillingState {
    data object Unavailable : BillingState
    data object Loading : BillingState
    data class Loaded(val products: List<ProductListing>) : BillingState
    data class Purchased(val result: PurchaseResult) : BillingState
    data class Error(val message: String) : BillingState
}
