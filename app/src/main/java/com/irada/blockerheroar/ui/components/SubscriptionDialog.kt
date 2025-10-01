package com.irada.blockerheroar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.irada.blockerheroar.R
import com.irada.blockerheroar.billing.BillingManager
import com.irada.blockerheroar.ui.theme.PremiumGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDialog(
    onDismiss: () -> Unit
) {
    var isPremium by remember { mutableStateOf(false) }
    var purchaseState by remember { mutableStateOf<com.irada.blockerheroar.billing.PurchaseState>(com.irada.blockerheroar.billing.PurchaseState.Idle) }
    
    // Try to get billing manager instance safely
    try {
        val billingManager = BillingManager.getInstance()
        isPremium = billingManager.isPremium.value
        purchaseState = billingManager.purchaseState.value
    } catch (e: Exception) {
        // BillingManager not initialized
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.premium_features),
                color = PremiumGold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.full_access),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(stringResource(R.string.custom_content_blocking))
                Text(stringResource(R.string.unlimited_blocking))
                Text(stringResource(R.string.app_deletion_prevention))
                Text(stringResource(R.string.new_apps_blocking))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.charity_support),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.subscription_price),
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumGold
                )
            }
        },
        confirmButton = {
                Button(
                    onClick = {
                    // Launch subscription flow
                    // This would need Activity context
                },
                enabled = !isPremium && purchaseState != com.irada.blockerheroar.billing.PurchaseState.Loading
            ) {
                if (purchaseState == com.irada.blockerheroar.billing.PurchaseState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(stringResource(R.string.subscribe))
                    }
                }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
            }
        }
    )
}