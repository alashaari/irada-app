package com.irada.blockerheroar.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.irada.blockerheroar.R

object PermissionManager {
    
    fun requestAccessibilityPermission(context: Context) {
        if (!isAccessibilityServiceEnabled(context)) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Toast.makeText(context, "يرجى تفعيل خدمة Irada في إعدادات إمكانية الوصول", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Fallback to app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Toast.makeText(context, "يرجى تفعيل خدمة Irada من إعدادات التطبيق", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    fun requestSystemAlertWindowPermission(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "يرجى السماح للتطبيق بالظهور فوق التطبيقات الأخرى", Toast.LENGTH_LONG).show()
        }
    }
    
    fun requestDeviceAdminPermission(context: Context) {
        val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, 
            com.irada.blockerheroar.receiver.DeviceAdminReceiver.getComponentName(context))
        intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
            "هذه الصلاحية مطلوبة لحماية التطبيق من الحذف")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains("${context.packageName}/${com.irada.blockerheroar.service.ContentBlockingService::class.java.name}") == true
    }
    
    fun isSystemAlertWindowEnabled(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun isDeviceAdminEnabled(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        return devicePolicyManager.isAdminActive(com.irada.blockerheroar.receiver.DeviceAdminReceiver.getComponentName(context))
    }
    
    fun checkAllPermissions(context: Context): Boolean {
        return isAccessibilityServiceEnabled(context) && 
               isSystemAlertWindowEnabled(context) && 
               isDeviceAdminEnabled(context)
    }
    
    fun requestPermissionsStepByStep(context: Context, onComplete: () -> Unit) {
        // Step 1: Request Accessibility Permission
        if (!isAccessibilityServiceEnabled(context)) {
            requestAccessibilityPermission(context)
            return
        }
        
        // Step 2: Request System Alert Window Permission
        if (!isSystemAlertWindowEnabled(context)) {
            requestSystemAlertWindowPermission(context)
            return
        }
        
        // Step 3: Request Device Admin Permission (optional)
        if (!isDeviceAdminEnabled(context)) {
            requestDeviceAdminPermission(context)
            return
        }
        
        // All permissions granted
        onComplete()
    }
    
    fun requestNotificationPermission(context: Context) {
        if (!isNotificationListenerEnabled(context)) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                
                Toast.makeText(context, 
                    "🔔 تفعيل مراقبة الإشعارات:\n" +
                    "1. ابحث عن 'Irada' أو 'BlockerHero'\n" +
                    "2. اضغط على التطبيق\n" +
                    "3. فعّل الخدمة",
                    Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "يرجى تفعيل مراقبة الإشعارات من إعدادات التطبيق", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains("${context.packageName}/${com.irada.blockerheroar.service.NotificationListenerService::class.java.name}") == true
    }
    
    fun showPermissionInstructions(context: Context) {
        Toast.makeText(context, 
            "لتفعيل هذه الميزة، يرجى:\n" +
            "1. الذهاب إلى الإعدادات\n" +
            "2. إمكانية الوصول\n" +
            "3. تفعيل خدمة Irada", 
            Toast.LENGTH_LONG).show()
    }
}
