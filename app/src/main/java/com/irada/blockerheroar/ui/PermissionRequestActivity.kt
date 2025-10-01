package com.irada.blockerheroar.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.irada.blockerheroar.R
import com.irada.blockerheroar.ui.theme.IradaTheme
import com.irada.blockerheroar.utils.PermissionManager
import com.irada.blockerheroar.utils.AccessibilityServiceHelper
import com.irada.blockerheroar.utils.EnhancedPermissionManager
import com.google.firebase.auth.FirebaseAuth

class PermissionRequestActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // All permissions granted, proceed to main app
            proceedToMainApp()
        } else {
            // Some permissions denied, show explanation
            showPermissionDeniedMessage()
        }
    }
    
    private val settingsResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check permissions again when returning from settings
        checkPermissionsAndProceed()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // التحقق من الصلاحيات أولاً قبل عرض الشاشة
        if (areAllCriticalPermissionsGranted()) {
            // الصلاحيات موجودة، انتقل مباشرة للتطبيق
            navigateToNextScreen()
            return
        }
        
        // الصلاحيات ناقصة، اعرض شاشة طلب الصلاحيات
        setContent {
            IradaTheme {
                PermissionRequestScreen(
                    onGrantPermissions = { requestPermissions() },
                    onSkipPermissions = { proceedToMainApp() }
                )
            }
        }
    }
    
    /**
     * التحقق من جميع الصلاحيات الحرجة
     */
    private fun areAllCriticalPermissionsGranted(): Boolean {
        // فحص الصلاحيات الأساسية
        val basicPermissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        
        val basicGranted = basicPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        // فحص الصلاحيات المتقدمة
        val accessibilityGranted = EnhancedPermissionManager.isAccessibilityServiceEnabled(this)
        val overlayGranted = EnhancedPermissionManager.isSystemAlertWindowEnabled(this)
        
        return basicGranted && accessibilityGranted && overlayGranted
    }
    
    /**
     * الانتقال للشاشة التالية (تسجيل الدخول أو الشاشة الرئيسية)
     */
    private fun navigateToNextScreen() {
        // التحقق من حالة تسجيل الدخول
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser != null) {
            // المستخدم مسجل دخول مسبقاً، انتقل للشاشة الرئيسية
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // المستخدم غير مسجل دخول، انتقل لشاشة تسجيل الدخول
            startActivity(Intent(this, com.irada.blockerheroar.ui.auth.LoginActivity::class.java))
        }
        finish()
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        requestPermissionLauncher.launch(permissions)
    }
    
    private fun proceedToMainApp() {
        // Request all required permissions using enhanced manager
        EnhancedPermissionManager.requestAllPermissions(
            activity = this,
            onComplete = {
                // All permissions requested, start login activity
                navigateToNextScreen()
            }
        )
    }
    
    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "بعض الصلاحيات مطلوبة لعمل التطبيق بشكل صحيح", Toast.LENGTH_LONG).show()
        // Still proceed to main app
        proceedToMainApp()
    }
    
    private fun checkPermissionsAndProceed() {
        // Check if all critical permissions are granted
        if (areAllCriticalPermissionsGranted()) {
            // Critical permissions granted, proceed to login
            navigateToNextScreen()
        } else {
            // Still need permissions, show status
            val status = EnhancedPermissionManager.getPermissionStatus(this)
            Toast.makeText(this, "حالة الصلاحيات:\n$status", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // التحقق من الصلاحيات عند العودة من الإعدادات فقط
        // onCreate يتعامل مع الحالة الأولى بالفعل
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestScreen(
    onGrantPermissions: () -> Unit,
    onSkipPermissions: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon
        Icon(
            Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = stringResource(R.string.permission_intro),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission List
        PermissionItem(
            icon = Icons.Default.Wifi,
            title = stringResource(R.string.internet_access),
            description = stringResource(R.string.internet_description)
        )
        
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.notifications),
            description = stringResource(R.string.notifications_description)
        )
        
        PermissionItem(
            icon = Icons.Default.Storage,
            title = stringResource(R.string.storage),
            description = stringResource(R.string.storage_description)
        )
        
        PermissionItem(
            icon = Icons.Default.Accessibility,
            title = stringResource(R.string.accessibility),
            description = stringResource(R.string.accessibility_description)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Grant Permissions Button
        Button(
            onClick = onGrantPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.grant_permissions))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip Button
        TextButton(
            onClick = onSkipPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.skip_now))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Note
        Text(
            text = stringResource(R.string.permission_note),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}