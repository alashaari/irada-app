package com.irada.blockerheroar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.irada.blockerheroar.R
import com.irada.blockerheroar.ui.auth.LoginActivity
import com.irada.blockerheroar.utils.AppPreferences
import com.irada.blockerheroar.utils.AdminManager
import com.irada.blockerheroar.utils.LanguageManager
import com.irada.blockerheroar.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = try {
        AppPreferences.getUser()
    } catch (e: Exception) {
        User() // Default user
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // User Information Section
        item {
            SettingsSection(
                title = stringResource(R.string.user_info)
            ) {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.user_name),
                    subtitle = user.name.ifEmpty { stringResource(R.string.not_specified) }
                )
                
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = stringResource(R.string.user_email),
                    subtitle = user.email.ifEmpty { stringResource(R.string.not_specified) }
                )
                
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.subscription_status),
                    subtitle = when {
                        AdminManager.isAdmin() -> stringResource(R.string.subscription_developer)
                        user.isPremium -> stringResource(R.string.subscription_premium)
                        else -> stringResource(R.string.subscription_free)
                    }
                )
            }
        }
        
        // Language Section
        item {
            SettingsSection(
                title = stringResource(R.string.app_preferences)
            ) {
                LanguageSelector()
            }
        }
        
        // App Information Section
        item {
            SettingsSection(
                title = stringResource(R.string.app_info)
            ) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.app_version),
                    subtitle = "1.0.0"
                )
                
                SettingsItem(
                    icon = Icons.Default.Support,
                    title = stringResource(R.string.technical_support),
                    subtitle = "support@irada.com",
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@irada.com")
                        }
                        context.startActivity(emailIntent)
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.rate_us),
                    subtitle = stringResource(R.string.rate_us_desc),
                    onClick = {
                        val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=com.irada.blockerheroar")
                        }
                        context.startActivity(playStoreIntent)
                    }
                )
            }
        }
        
        // Account Section
        item {
            SettingsSection(
                title = stringResource(R.string.account)
            ) {
                SettingsItem(
                    icon = Icons.Default.ExitToApp,
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.logout_desc),
                    onClick = {
                        auth.signOut()
                        AppPreferences.clearUser()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageSelector() {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(AppPreferences.getLanguage()) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    SettingsItem(
        icon = Icons.Default.Language,
        title = stringResource(R.string.language),
        subtitle = if (currentLanguage == "ar") 
            stringResource(R.string.arabic) 
        else 
            stringResource(R.string.english),
        onClick = { showLanguageDialog = true }
    )
    
    // Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
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
                                    showLanguageDialog = false
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
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}