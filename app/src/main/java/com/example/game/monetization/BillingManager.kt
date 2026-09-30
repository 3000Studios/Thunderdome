package com.example.game.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class BillingState {
    object Disconnected : BillingState()
    object Connecting : BillingState()
    object Connected : BillingState()
    data class Error(val message: String) : BillingState()
}

data class ProductDetailItem(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val productType: String, // INAPP
    val productDetails: ProductDetails? = null
)

class BillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPurchaseSuccess: (productId: String) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_REMOVE_ADS = "remove_ads"
        const val PRODUCT_STARTER_PACK = "starter_pack"
        const val PRODUCT_FOUNDER_PACK = "founder_pack"
        const val PRODUCT_CORES_SMALL = "plasma_cores_small"
        const val PRODUCT_CORES_LARGE = "plasma_cores_large"

        val ALL_INAPP_PRODUCTS = listOf(
            PRODUCT_REMOVE_ADS,
            PRODUCT_STARTER_PACK,
            PRODUCT_FOUNDER_PACK,
            PRODUCT_CORES_SMALL,
            PRODUCT_CORES_LARGE
        )
    }

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val billingState: StateFlow<BillingState> = _billingState

    private val _availableProducts = MutableStateFlow<Map<String, ProductDetailItem>>(
        // Default platform display fallbacks when offline / pre-Play Console listing
        mapOf(
            PRODUCT_REMOVE_ADS to ProductDetailItem(
                PRODUCT_REMOVE_ADS,
                "Remove Ads",
                "Permanently disables all interstitial ads and grants instant continue.",
                "$2.99",
                BillingClient.ProductType.INAPP
            ),
            PRODUCT_STARTER_PACK to ProductDetailItem(
                PRODUCT_STARTER_PACK,
                "Starter Pilot Pack",
                "Unlocks BlazeHound fighter, 15,000 credits & 30 plasma cores.",
                "$4.99",
                BillingClient.ProductType.INAPP
            ),
            PRODUCT_FOUNDER_PACK to ProductDetailItem(
                PRODUCT_FOUNDER_PACK,
                "3000 Studios Founder Pack",
                "Exclusive Gold Apex Zero craft, Founder decal, 50k credits, 100 cores & No Ads.",
                "$9.99",
                BillingClient.ProductType.INAPP
            ),
            PRODUCT_CORES_SMALL to ProductDetailItem(
                PRODUCT_CORES_SMALL,
                "25 Plasma Cores",
                "Supply drop of 25 Plasma Cores for overclocking and unlocking fighters.",
                "$0.99",
                BillingClient.ProductType.INAPP
            ),
            PRODUCT_CORES_LARGE to ProductDetailItem(
                PRODUCT_CORES_LARGE,
                "150 Plasma Cores",
                "Fleet reserve crate containing 150 Plasma Cores.",
                "$4.99",
                BillingClient.ProductType.INAPP
            )
        )
    )
    val availableProducts: StateFlow<Map<String, ProductDetailItem>> = _availableProducts

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        startConnection()
    }

    fun startConnection() {
        _billingState.value = BillingState.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingState.value = BillingState.Connected
                    queryProducts()
                    restorePurchases()
                } else {
                    _billingState.value = BillingState.Error("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
            }
        })
    }

    private fun queryProducts() {
        val productList = ALL_INAPP_PRODUCTS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val updated = _availableProducts.value.toMutableMap()
                for (pd in productDetailsList) {
                    val oneTimeOffer = pd.oneTimePurchaseOfferDetails
                    val price = oneTimeOffer?.formattedPrice ?: "$0.00"
                    updated[pd.productId] = ProductDetailItem(
                        productId = pd.productId,
                        title = pd.title,
                        description = pd.description,
                        formattedPrice = price,
                        productType = pd.productType,
                        productDetails = pd
                    )
                }
                _availableProducts.value = updated
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String) {
        val item = _availableProducts.value[productId]
        val pd = item?.productDetails

        if (pd != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(pd)
                    .build()
            )

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        } else {
            // Sideloaded / Sandbox Mock Test fallback when running outside Play Store
            coroutineScope.launch {
                handleMockTestPurchase(productId)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // User cancelled
        } else {
            _billingState.value = BillingState.Error("Purchase failed: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            for (productId in purchase.products) {
                // If consumable (plasma cores), consume it
                if (productId == PRODUCT_CORES_SMALL || productId == PRODUCT_CORES_LARGE) {
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.consumeAsync(consumeParams) { _, _ ->
                        onPurchaseSuccess(productId)
                    }
                } else {
                    // Non-consumable (Remove Ads, Starter, Founder): Acknowledge purchase
                    if (!purchase.isAcknowledged) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(acknowledgeParams) { _ ->
                            onPurchaseSuccess(productId)
                        }
                    } else {
                        onPurchaseSuccess(productId)
                    }
                }
            }
        }
    }

    fun restorePurchases() {
        if (!billingClient.isReady) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        for (pid in purchase.products) {
                            onPurchaseSuccess(pid)
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleMockTestPurchase(productId: String) {
        withContext(Dispatchers.Main) {
            onPurchaseSuccess(productId)
        }
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
