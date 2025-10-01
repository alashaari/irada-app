package com.irada.blockerheroar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.irada.blockerheroar.R
import com.irada.blockerheroar.data.AppSettings
import com.irada.blockerheroar.utils.AppPreferences
import com.irada.blockerheroar.utils.PermissionManager
import com.irada.blockerheroar.utils.AdminManager
import com.irada.blockerheroar.utils.AccessibilityServiceHelper
import com.irada.blockerheroar.utils.LanguageManager
import com.irada.blockerheroar.utils.PartnerManager
import com.irada.blockerheroar.service.BackgroundMonitoringService
import com.irada.blockerheroar.ui.components.SubscriptionDialog
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var settings by remember { 
        mutableStateOf(
            try {
                AppPreferences.getSettings()
            } catch (e: Exception) {
                AppSettings()
            }
        )
    }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // دالة مساعدة للتحقق من الشريك قبل تعطيل الميزة
    suspend fun handleFeatureToggle(
        featureName: String,
        currentState: Boolean,
        onApproved: () -> Unit
    ) {
        val partnerEmail = PartnerManager.getPartnerEmail()
        
        // إذا المستخدم يبغى يعطّل الميزة
        if (currentState) {
            // تحقق إذا في شريك
            if (partnerEmail.isNotEmpty()) {
                // أرسل طلب موافقة للشريك
                val success = PartnerManager.sendApprovalRequest(featureName, "disable")
                if (success) {
                    Toast.makeText(
                        context, 
                        "تم إرسال طلب الموافقة للشريك عبر الإيميل",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "فشل في إرسال الطلب للشريك",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // لا يوجد شريك، اسمح بالتعطيل مباشرة
                onApproved()
            }
        } else {
            // المستخدم يبغى يفعّل الميزة، اسمح مباشرة
            onApproved()
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Subscribe and Language buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showLanguageDialog = true }
                ) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.language))
                }
                
                Button(
                    onClick = { showSubscriptionDialog = true }
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.subscribe))
                }
            }
        }
        
        // Content Blocking Section
        item {
            FeatureSection(
                title = stringResource(R.string.content_blocking_title),
                features = listOf(
                    FeatureItem(
                        title = stringResource(R.string.block_pornographic_content),
                        description = stringResource(R.string.block_pornographic_content_desc),
                        isEnabled = settings.blockPornographicContent,
                        isPremium = false,
                        onToggle = { enabled ->
                            scope.launch {
                                handleFeatureToggle(
                                    featureName = "block_pornographic_content",
                                    currentState = settings.blockPornographicContent
                                ) {
                                    if (enabled) {
                                        AccessibilityServiceHelper.checkAndEnableService(
                                            context = context,
                                            onSuccess = {
                                                settings = settings.copy(blockPornographicContent = enabled)
                                                AppPreferences.updateSetting("block_porn", enabled)
                                                Toast.makeText(context, "تم تفعيل حظر المحتوى الإباحي بنجاح!", Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = {
                                                settings = settings.copy(blockPornographicContent = enabled)
                                                AppPreferences.updateSetting("block_porn", enabled)
                                                Toast.makeText(context, "تم تفعيل الميزة! يرجى تفعيل الخدمة من الإعدادات", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        settings = settings.copy(blockPornographicContent = enabled)
                                        AppPreferences.updateSetting("block_porn", enabled)
                                        Toast.makeText(context, "تم إلغاء تفعيل حظر المحتوى الإباحي", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ),
                    FeatureItem(
                        title = stringResource(R.string.block_youtube_shorts),
                        description = stringResource(R.string.block_youtube_shorts_desc),
                        isEnabled = settings.blockYouTubeShorts,
                        isPremium = true,
                        onToggle = { enabled ->
                            scope.launch {
                                handleFeatureToggle(
                                    featureName = "block_youtube_shorts",
                                    currentState = settings.blockYouTubeShorts
                                ) {
                                    if (enabled) {
                                        if (AdminManager.canAccessPremiumFeature("block_youtube_shorts")) {
                                            AccessibilityServiceHelper.checkAndEnableService(
                                                context = context,
                                                onSuccess = {
                                                    PermissionManager.requestNotificationPermission(context)
                                                    settings = settings.copy(blockYouTubeShorts = enabled)
                                                    AppPreferences.updateSetting("block_youtube_shorts", enabled)
                                                    BackgroundMonitoringService.startService(context)
                                                    Toast.makeText(context, "تم تفعيل حظر ريلز اليوتيوب!", Toast.LENGTH_LONG).show()
                                                },
                                                onFailure = {
                                                    settings = settings.copy(blockYouTubeShorts = enabled)
                                                    AppPreferences.updateSetting("block_youtube_shorts", enabled)
                                                    Toast.makeText(context, "تم تفعيل الميزة! يرجى تفعيل الخدمة من الإعدادات", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        } else {
                                            showSubscriptionDialog = true
                                        }
                                    } else {
                                        settings = settings.copy(blockYouTubeShorts = enabled)
                                        AppPreferences.updateSetting("block_youtube_shorts", enabled)
                                        BackgroundMonitoringService.stopService(context)
                                        Toast.makeText(context, "تم إلغاء تفعيل حظر ريلز اليوتيوب", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ),
                    FeatureItem(
                        title = stringResource(R.string.block_snapchat_stories),
                        description = stringResource(R.string.block_snapchat_stories_desc),
                        isEnabled = settings.blockSnapchatStories,
                        isPremium = true,
                        onToggle = { enabled ->
                            scope.launch {
                                handleFeatureToggle(
                                    featureName = "block_snapchat_stories",
                                    currentState = settings.blockSnapchatStories
                                ) {
                                    if (enabled) {
                                        if (AdminManager.canAccessPremiumFeature("block_snapchat_stories")) {
                                            settings = settings.copy(blockSnapchatStories = enabled)
                                            AppPreferences.updateSetting("block_snapchat", enabled)
                                            Toast.makeText(context, "تم تفعيل حظر قصص سناب شات", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showSubscriptionDialog = true
                                        }
                                    } else {
                                        settings = settings.copy(blockSnapchatStories = enabled)
                                        AppPreferences.updateSetting("block_snapchat", enabled)
                                        Toast.makeText(context, "تم إلغاء تفعيل حظر قصص سناب شات", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ),
                    FeatureItem(
                        title = stringResource(R.string.block_instagram_explore),
                        description = stringResource(R.string.block_instagram_explore_desc),
                        isEnabled = settings.blockInstagramExplore,
                        isPremium = true,
                        onToggle = { enabled ->
                            if (enabled) {
                                showSubscriptionDialog = true
                            } else {
                                scope.launch {
                                    handleFeatureToggle(
                                        featureName = "block_instagram_explore",
                                        currentState = settings.blockInstagramExplore
                                    ) {
                                        settings = settings.copy(blockInstagramExplore = enabled)
                                        AppPreferences.updateSetting("block_instagram", enabled)
                                        Toast.makeText(context, "تم إلغاء تفعيل حظر استكشاف انستقرام", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ),
                    FeatureItem(
                        title = stringResource(R.string.block_telegram_search),
                        description = stringResource(R.string.block_telegram_search_desc),
                        isEnabled = settings.blockTelegramSearch,
                        isPremium = true,
                        onToggle = { enabled ->
                            if (enabled) {
                                showSubscriptionDialog = true
                            } else {
                                scope.launch {
                                    handleFeatureToggle(
                                        featureName = "block_telegram_search",
                                        currentState = settings.blockTelegramSearch
                                    ) {
                                        settings = settings.copy(blockTelegramSearch = enabled)
                                        AppPreferences.updateSetting("block_telegram", enabled)
                                        Toast.makeText(context, "تم إلغاء تفعيل حظر بحث تيليجرام", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                )
            )
        }
        
        // Blocking Screen Settings
        item {
            FeatureSection(
                title = stringResource(R.string.blocking_screen_title),
                features = listOf(
                    FeatureItem(
                        title = stringResource(R.string.blocking_message),
                        description = stringResource(R.string.blocking_message_desc),
                        isEnabled = true,
                        isPremium = false,
                        onToggle = { }
                    )
                )
            )
        }
        
        // App Protection
        item {
            var showPartnerDialog by remember { mutableStateOf(false) }
            var currentPartnerEmail by remember { mutableStateOf(settings.partnerEmail) }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureSection(
                    title = stringResource(R.string.app_protection_title),
                    features = listOf(
                        FeatureItem(
                            title = stringResource(R.string.prevent_app_deletion),
                            description = stringResource(R.string.prevent_app_deletion_desc),
                            isEnabled = settings.preventAppDeletion,
                            isPremium = true,
                            onToggle = { enabled ->
                                if (enabled) {
                                    PermissionManager.requestDeviceAdminPermission(context)
                                    showSubscriptionDialog = true
                                } else {
                                    scope.launch {
                                        handleFeatureToggle(
                                            featureName = "prevent_app_deletion",
                                            currentState = settings.preventAppDeletion
                                        ) {
                                            settings = settings.copy(preventAppDeletion = enabled)
                                            AppPreferences.updateSetting("prevent_deletion", enabled)
                                            Toast.makeText(context, "تم إلغاء تفعيل منع حذف التطبيق", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                    )
                )
                
                // Partner Email Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPartnerDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.partner_title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Premium",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = if (currentPartnerEmail.isEmpty()) 
                                    stringResource(R.string.partner_email_desc)
                                else 
                                    currentPartnerEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            if (currentPartnerEmail.isEmpty()) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Partner Email Dialog
            if (showPartnerDialog) {
                PartnerEmailDialog(
                    currentEmail = currentPartnerEmail,
                    onDismiss = { showPartnerDialog = false },
                    onSave = { email ->
                        scope.launch {
                            if (email.isNotEmpty()) {
                                val success = PartnerManager.addPartner(email)
                                if (success) {
                                    currentPartnerEmail = email
                                    settings = settings.copy(partnerEmail = email)
                                    Toast.makeText(context, "تم إضافة الشريك بنجاح وسيتم إرسال إيميل له", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "فشل في إضافة الشريك - تحقق من صحة الإيميل", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                val success = PartnerManager.removePartner()
                                if (success) {
                                    currentPartnerEmail = ""
                                    settings = settings.copy(partnerEmail = "")
                                    Toast.makeText(context, "تم إزالة الشريك", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showPartnerDialog = false
                    }
                )
            }
        }
        
        // Advanced Features
        item {
            FeatureSection(
                title = stringResource(R.string.advanced_features_title),
                features = listOf(
                    FeatureItem(
                        title = stringResource(R.string.block_new_apps),
                        description = stringResource(R.string.block_new_apps_desc),
                        isEnabled = settings.blockNewApps,
                        isPremium = true,
                        onToggle = { enabled ->
                            if (enabled) {
                                showSubscriptionDialog = true
                            } else {
                                scope.launch {
                                    handleFeatureToggle(
                                        featureName = "block_new_apps",
                                        currentState = settings.blockNewApps
                                    ) {
                                        settings = settings.copy(blockNewApps = enabled)
                                        AppPreferences.updateSetting("block_new_apps", enabled)
                                        Toast.makeText(context, "تم إلغاء تفعيل حظر التطبيقات الجديدة", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ),
                    FeatureItem(
                        title = stringResource(R.string.prevent_factory_reset),
                        description = stringResource(R.string.prevent_factory_reset_desc),
                        isEnabled = settings.preventFactoryReset,
                        isPremium = true,
                        onToggle = { enabled ->
                            if (enabled) {
                                showSubscriptionDialog = true
                            } else {
                                scope.launch {
                                    handleFeatureToggle(
                                        featureName = "prevent_factory_reset",
                                        currentState = settings.preventFactoryReset
                                    ) {
                                        settings = settings.copy(preventFactoryReset = enabled)
                                        AppPreferences.updateSetting("prevent_factory_reset", enabled)
                                        Toast.makeText(context, "تم إلغاء تفعيل منع إعادة ضبط المصنع", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                )
            )
        }
    }
    
    if (showSubscriptionDialog) {
        SubscriptionDialog(
            onDismiss = { showSubscriptionDialog = false }
        )
    }
    
    if (showLanguageDialog) {
        WorkingLanguageDialog(
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun WorkingLanguageDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(AppPreferences.getLanguage()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                listOf(
                    "ar" to stringResource(R.string.arabic),
                    "en" to stringResource(R.string.english)
                ).forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (code != currentLanguage) {
                                    LanguageManager.changeLanguage(context, code, true)
                                }
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = {
                                if (code != currentLanguage) {
                                    LanguageManager.changeLanguage(context, code, true)
                                }
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.partner_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.partner_email_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.partner_email)) },
                    placeholder = { Text("partner@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (currentEmail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { 
                            email = ""
                        }
                    ) {
                        Text("إزالة الشريك الحالي")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(email.trim()) },
                enabled = email.trim() != currentEmail
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun FeatureSection(
    title: String,
    features: List<FeatureItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            features.forEach { feature ->
                FeatureItemCard(feature)
                if (feature != features.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FeatureItemCard(feature: FeatureItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (feature.isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    if (feature.isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = feature.isEnabled,
                onCheckedChange = feature.onToggle,
                enabled = true
            )
        }
    }
}

data class FeatureItem(
    val title: String,
    val description: String,
    val isEnabled: Boolean,
    val isPremium: Boolean,
    val onToggle: (Boolean) -> Unit
)