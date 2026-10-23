package com.nexus.domain.repository

import com.nexus.domain.model.BillingState
import com.nexus.domain.model.ProductTier
import kotlinx.coroutines.flow.Flow

interface IBillingRepository {
    val billingState: Flow<BillingState>
    suspend fun initialize()
    suspend fun purchase(tier: ProductTier, activity: android.app.Activity)
    suspend fun acknowledgePurchase(purchaseToken: String, sku: String)
    suspend fun queryPurchases(): List<String>
    fun isPro(): Flow<Boolean>
}
