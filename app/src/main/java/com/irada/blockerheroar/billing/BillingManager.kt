package com.irada.blockerheroar.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager private constructor() : BillingClientStateListener, PurchasesUpdatedListener {
    
    companion object {
        @Volatile
        private var INSTANCE: BillingManager? = null
        
        fun initialize(context: Context): BillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager().also { INSTANCE = it }
            }
        }
        
        fun getInstance(): BillingManager {
            return INSTANCE ?: throw IllegalStateException("BillingManager not initialized")
        }
    }
    
    private lateinit var billingClient: BillingClient
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()
    
    private val _billingState = MutableStateFlow(BillingState.NOT_INITIALIZED)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private var purchaseCallback: ((Boolean) -> Unit)? = null
    
    fun initialize(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        startConnection()
    }
    
    private fun startConnection() {
        billingClient.startConnection(this)
    }
    
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _billingState.value = BillingState.READY
            queryPurchases()
        } else {
            _billingState.value = BillingState.ERROR
        }
    }
    
    override fun onBillingServiceDisconnected() {
        _billingState.value = BillingState.DISCONNECTED
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { handlePurchases(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error
            }
        }
    }
    
    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
                _isPremium.value = true
                _purchaseState.value = PurchaseState.Success
                purchaseCallback?.invoke(true)
            }
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            // Handle acknowledgment result
        }
    }
    
    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _isPremium.value = purchases.isNotEmpty()
            }
        }
    }
    
    fun launchSubscriptionFlow(activity: Activity, callback: (Boolean) -> Unit) {
        purchaseCallback = callback
        _purchaseState.value = PurchaseState.Loading
        
        // Query product details first
        val productList = listOf("premium_subscription")
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList.map { 
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            })
            .build()
        
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                
                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                _purchaseState.value = PurchaseState.Error
                callback(false)
            }
        }
    }
    
    
    fun endConnection() {
        billingClient.endConnection()
    }
}

enum class BillingState {
    NOT_INITIALIZED,
    READY,
    ERROR,
    DISCONNECTED
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    object Error : PurchaseState()
}
