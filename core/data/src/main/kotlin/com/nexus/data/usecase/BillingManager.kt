package com.nexus.data.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.nexus.domain.model.BillingState
import com.nexus.domain.model.ProductListing
import com.nexus.domain.model.ProductTier
import com.nexus.domain.model.PurchaseResult
import com.nexus.domain.repository.IBillingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : IBillingRepository {

    private var billingClient: BillingClient? = null

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Loading)
    override val billingState: Flow<BillingState> = _billingState.asStateFlow()

    private val skuMapping = mapOf(
        "nexus_pro_season" to ProductTier.PRO_SEASON,
        "nexus_pro_monthly" to ProductTier.PRO_MONTHLY,
        "nexus_pro_yearly" to ProductTier.PRO_YEARLY,
        "nexus_lifetime" to ProductTier.LIFETIME,
    )

    override suspend fun initialize() {
        if (billingClient?.isReady == true) return

        val client = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    purchases.forEach { purchase ->
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            val sku = purchase.products.firstOrNull() ?: return@forEach
                            _billingState.value = BillingState.Purchased(
                                PurchaseResult(
                                    sku = sku,
                                    purchaseToken = purchase.purchaseToken,
                                    isAcknowledged = purchase.isAcknowledged
                                )
                            )
                            if (!purchase.isAcknowledged) {
                                val ackParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                billingClient?.acknowledgePurchase(ackParams) { ackResult: BillingResult ->
                                    if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                                        Timber.w("Acknowledge failed: ${ackResult.debugMessage}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .build()

        billingClient = client

        val connected = suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.d("Billing connected")
                        continuation.resume(true)
                    } else {
                        Timber.w("Billing setup failed: ${billingResult.debugMessage}")
                        _billingState.value = BillingState.Unavailable
                        continuation.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.w("Billing disconnected")
                    _billingState.value = BillingState.Unavailable
                }
            })
        }
        if (!connected) return
        // Prices used to be fetched only inside purchase(), i.e. after the
        // student had already decided — so the paywall could never show a
        // real number and had nothing to sell against.
        _billingState.value = BillingState.Loaded(loadCatalogue(client))
    }

    /** One request for every SKU. A plan Play does not price is left out of the
     *  list, which the paywall renders as unavailable rather than guessed at. */
    private suspend fun loadCatalogue(client: BillingClient): List<ProductListing> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                skuMapping.keys.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                },
            )
            .build()

        val (result, details) = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { continuation ->
            client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                continuation.resume(billingResult to productDetailsList)
            }
        }

        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.w("Catalogue query failed: ${result.debugMessage}")
            return emptyList()
        }
        return details.mapNotNull(::toListing)
    }

    private fun toListing(details: ProductDetails): ProductListing? {
        val tier = skuMapping[details.productId] ?: return null
        // Subscriptions and one-time products hang their price off different
        // objects, and the lifetime SKU has been registered as either.
        val phase = details.subscriptionOfferDetails
            ?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()
        val oneTime = details.oneTimePurchaseOfferDetails
        val micros = phase?.priceAmountMicros ?: oneTime?.priceAmountMicros ?: return null
        val currency = phase?.priceCurrencyCode ?: oneTime?.priceCurrencyCode ?: return null
        val formatted = phase?.formattedPrice ?: oneTime?.formattedPrice ?: return null
        return ProductListing(
            sku = details.productId,
            tier = tier,
            title = details.title,
            description = details.description,
            price = formatted,
            priceMicros = micros,
            currencyCode = currency,
        )
    }

    override suspend fun purchase(tier: ProductTier, activity: android.app.Activity) {
        val client = billingClient ?: run {
            _billingState.value = BillingState.Error("Billing not initialized")
            return
        }

        val sku = skuMapping.entries.firstOrNull { it.value == tier }?.key ?: run {
            _billingState.value = BillingState.Error("Unknown tier: $tier")
            return
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val productDetailsResult = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { continuation ->
            client.queryProductDetailsAsync(params) { billingResult, details ->
                continuation.resume(Pair(billingResult, details))
            }
        }

        val (queryBillingResult, productDetailsList) = productDetailsResult
        if (queryBillingResult.responseCode != BillingClient.BillingResponseCode.OK || productDetailsList.isEmpty()) {
            _billingState.value = BillingState.Error("Failed to load product details")
            return
        }

        val productDetails = productDetailsList.first()
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: run {
            _billingState.value = BillingState.Error("No subscription offer found")
            return
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        val billingResult = client.launchBillingFlow(activity, flowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.w("Billing flow failed: ${billingResult.responseCode}")
        }
    }

    override suspend fun acknowledgePurchase(purchaseToken: String, sku: String) {
        val client = billingClient ?: return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        suspendCancellableCoroutine<Unit> { continuation ->
            client.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Timber.w("Acknowledge failed: ${billingResult.debugMessage}")
                }
                continuation.resume(Unit)
            }
        }
    }

override suspend fun queryPurchases(): List<String> {
val client = billingClient ?: return emptyList()
return try {
suspendCancellableCoroutine { continuation ->
client.queryPurchasesAsync(
QueryPurchasesParams.newBuilder()
.setProductType(BillingClient.ProductType.SUBS)
.build()
) { billingResult, purchasesList ->
if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
val ownedSkus = purchasesList
    .mapNotNull { it.products.firstOrNull() }
continuation.resume(ownedSkus)
} else {
Timber.w("queryPurchases failed: ${billingResult.debugMessage}")
continuation.resume(emptyList())
}
}
}
} catch (e: Exception) {
Timber.e(e, "queryPurchases exception")
emptyList()
}
}

override fun isPro(): Flow<Boolean> = callbackFlow {
val client = billingClient ?: run { trySend(false); awaitClose(); return@callbackFlow }
client.queryPurchasesAsync(
QueryPurchasesParams.newBuilder()
.setProductType(BillingClient.ProductType.SUBS)
.build()
) { billingResult, purchasesList ->
if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
val hasPro = purchasesList.any { purchase ->
purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
(purchase.products.contains("nexus_pro_season") ||
purchase.products.contains("nexus_pro_monthly") ||
purchase.products.contains("nexus_pro_yearly") ||
purchase.products.contains("nexus_lifetime"))
}
trySend(hasPro)
} else {
trySend(false)
}
}
awaitClose()
}
}
