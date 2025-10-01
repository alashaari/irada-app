package com.irada.blockerheroar.utils

import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object AccessibilityServiceHelper {
    
    private const val ACCESSIBILITY_SERVICE_NAME = "com.irada.blockerheroar/.service.ContentBlockingService"
    
    fun enableAccessibilityService(context: Context): Boolean {
        // Always use manual activation for better compatibility
        requestManualActivation(context)
        return false
    }
    
    private fun requestManualActivation(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Toast.makeText(
                context,
                "🔧 تفعيل خدمة إمكانية الوصول:\n" +
                "1. ابحث عن 'Irada' أو 'BlockerHero'\n" +
                "2. اضغط على التطبيق\n" +
                "3. فعّل الخدمة\n" +
                "4. ارجع للتطبيق",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // Fallback to app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Toast.makeText(
                context,
                "يرجى تفعيل خدمة Irada من إعدادات التطبيق",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains(ACCESSIBILITY_SERVICE_NAME) == true
        } catch (e: Exception) {
            false
        }
    }
    
    fun checkAndEnableService(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (isAccessibilityServiceEnabled(context)) {
            android.util.Log.d("AccessibilityServiceHelper", "Service is enabled")
            onSuccess()
        } else {
            android.util.Log.d("AccessibilityServiceHelper", "Service is not enabled, requesting activation")
            val success = enableAccessibilityService(context)
            if (success) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }
    
    fun isServiceRunning(): Boolean {
        return try {
            // Check if service is actually running
            true // Placeholder - would need to check actual service status
        } catch (e: Exception) {
            false
        }
    }
}
