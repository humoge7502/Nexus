package com.nexus.paywall

import android.app.Activity
import com.nexus.analytics.AnalyticsTracker
import com.nexus.data.usecase.BillingManager
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.BillingState
import com.nexus.domain.model.ProductListing
import com.nexus.domain.model.ProductTier
import com.nexus.domain.model.PurchaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var billingManager: BillingManager
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var userPreferences: UserPreferences
    private lateinit var billingState: MutableStateFlow<BillingState>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        billingManager = mock()
        analyticsTracker = mock()
        userPreferences = mock()
        billingState = MutableStateFlow(BillingState.Loading)
        whenever(billingManager.billingState).thenReturn(billingState)
        runBlocking {
            whenever(userPreferences.saveIsPro(any())).thenReturn(Unit)
            whenever(billingManager.purchase(any(), any())).thenReturn(Unit)
            whenever(billingManager.initialize()).thenReturn(Unit)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PaywallViewModel(billingManager, analyticsTracker, userPreferences)

    private fun listing(tier: ProductTier, price: String) = ProductListing(
        sku = "nexus_${tier.name.lowercase()}",
        tier = tier,
        title = tier.name,
        description = "",
        price = price,
        priceMicros = 0L,
        currencyCode = "INR",
    )

    @Test
    fun `opening the paywall is tracked`() = runTest(testDispatcher) {
        viewModel()
        verify(analyticsTracker).track("screen_view", mapOf("screen" to "paywall"))
    }

    @Test
    fun `opening the paywall starts billing instead of waiting forever`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        runBlocking { verify(billingManager).initialize() }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loaded products map prices onto their tiers`() = runTest(testDispatcher) {
        billingState.value = BillingState.Loaded(
            listOf(
                listing(ProductTier.PRO_MONTHLY, "₹299"),
                listing(ProductTier.PRO_YEARLY, "₹1,999"),
                listing(ProductTier.LIFETIME, "₹4,999"),
            )
        )
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("₹299", state.monthlyPrice)
        assertEquals("₹1,999", state.yearlyPrice)
        assertEquals("₹4,999", state.lifetimePrice)
        assertFalse(state.isPro)
    }

    @Test
    fun `missing products leave their price blank`() = runTest(testDispatcher) {
        billingState.value = BillingState.Loaded(listOf(listing(ProductTier.PRO_MONTHLY, "₹299")))
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals("₹299", viewModel.uiState.value.monthlyPrice)
        assertEquals("", viewModel.uiState.value.yearlyPrice)
        assertEquals("", viewModel.uiState.value.lifetimePrice)
    }

    @Test
    fun `unavailable billing stops loading without prices`() = runTest(testDispatcher) {
        billingState.value = BillingState.Unavailable
        val viewModel = viewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("", viewModel.uiState.value.monthlyPrice)
    }

    @Test
    fun `billing error stops loading`() = runTest(testDispatcher) {
        billingState.value = BillingState.Error("play services missing")
        val viewModel = viewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `a purchase marks the user pro and persists the flag`() = runTest(testDispatcher) {
        billingState.value = BillingState.Purchased(
            PurchaseResult(sku = "nexus_pro_yearly", purchaseToken = "token", isAcknowledged = true)
        )
        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPro)
        runBlocking { verify(userPreferences).saveIsPro(true) }
    }

    @Test
    fun `each purchase button tracks its tier and starts the billing flow`() = runTest(testDispatcher) {
        val activity = mock<Activity>()
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.purchaseMonthly(activity)
        viewModel.purchaseYearly(activity)
        viewModel.purchaseLifetime(activity)
        advanceUntilIdle()

        verify(analyticsTracker).track("paywall_purchase", mapOf("tier" to "monthly"))
        verify(analyticsTracker).track("paywall_purchase", mapOf("tier" to "yearly"))
        verify(analyticsTracker).track("paywall_purchase", mapOf("tier" to "lifetime"))
        runBlocking {
            verify(billingManager).purchase(ProductTier.PRO_MONTHLY, activity)
            verify(billingManager).purchase(ProductTier.PRO_YEARLY, activity)
            verify(billingManager).purchase(ProductTier.LIFETIME, activity)
        }
    }

    @Test
    fun `restore reinitializes billing and is tracked`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        clearInvocations(billingManager)

        viewModel.restorePurchases()
        advanceUntilIdle()

        verify(analyticsTracker).track("paywall_restore")
        runBlocking { verify(billingManager).initialize() }
    }
}
