package com.irada.blockerheroar.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.irada.blockerheroar.R
import com.irada.blockerheroar.receiver.DeviceAdminReceiver
import com.irada.blockerheroar.service.ContentBlockingService

class EnhancedPermissionManager {
    
    companion object {
        private const val REQUEST_CODE_ACCESSIBILITY = 1001
        private const val REQUEST_CODE_OVERLAY = 1002
        private const val REQUEST_CODE_DEVICE_ADMIN = 1003
        private const val REQUEST_CODE_NOTIFICATION = 1004
        
        private var permissionCallback: (() -> Unit)? = null
        private var currentStep = 0
        private val totalSteps = 4
        
        fun requestAllPermissions(activity: Activity, onComplete: () -> Unit) {
            permissionCallback = onComplete
            currentStep = 1
            requestNextPermission(activity)
        }
        
        private fun requestNextPermission(activity: Activity) {
            when (currentStep) {
                1 -> requestAccessibilityPermissionWithRetry(activity)
                2 -> requestSystemAlertWindowPermission(activity)
                3 -> requestDeviceAdminPermission(activity)
                4 -> requestNotificationPermission(activity)
                else -> {
                    // All permissions requested
                    permissionCallback?.invoke()
                }
            }
        }
        
        // Enhanced accessibility permission request with better error handling
        private fun requestAccessibilityPermissionWithRetry(activity: Activity) {
            try {
                // First, check if service is already enabled
                if (isAccessibilityServiceEnabled(activity)) {
                    android.util.Log.d("Permission", "Accessibility already enabled")
                    currentStep++
                    requestNextPermission(activity)
                    return
                }
                
                // Show detailed instructions
                val instructions = """
                    خطوات تفعيل خدمة إمكانية الوصول:
                    
                    1. ستفتح صفحة إعدادات إمكانية الوصول
                    2. ابحث عن "Irada" في القائمة
                    3. اضغط على "Irada" 
                    4. قم بتفعيل الخدمة
                    5. اضغط "موافق" على رسالة التأكيد
                    6. ارجع للتطبيق
                    
                    ملاحظة: هذه الخطوة ضرورية لمراقبة اليوتيوب
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle("تفعيل خدمة إمكانية الوصول")
                    .setMessage(instructions)
                    .setPositiveButton("فتح الإعدادات") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            activity.startActivity(intent)
                            
                            // Show toast with additional guidance
                            Toast.makeText(activity, "ابحث عن 'Irada' وقم بتفعيله", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            android.util.Log.e("Permission", "Error opening accessibility settings", e)
                            // Fallback to app settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:${activity.packageName}")
                            activity.startActivity(intent)
                        }
                    }
                    .setNegativeButton("تخطي") { _, _ ->
                        currentStep++
                        requestNextPermission(activity)
                    }
                    .setCancelable(false)
                    .show()
                
            } catch (e: Exception) {
                android.util.Log.e("Permission", "Error requesting accessibility permission", e)
                currentStep++
                requestNextPermission(activity)
            }
        }
        
        private fun requestSystemAlertWindowPermission(activity: Activity) {
            if (isSystemAlertWindowEnabled(activity)) {
                currentStep++
                requestNextPermission(activity)
                return
            }
            
            showPermissionDialog(
                activity = activity,
                title = "🪟 تفعيل الظهور فوق التطبيقات",
                message = "هذه الصلاحية مطلوبة لإظهار شاشة الحظر فوق التطبيقات الأخرى",
                action = {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:${activity.packageName}")
                    activity.startActivity(intent)
                    Toast.makeText(activity, "يرجى السماح للتطبيق بالظهور فوق التطبيقات الأخرى", Toast.LENGTH_LONG).show()
                },
                onNext = {
                    currentStep++
                    requestNextPermission(activity)
                }
            )
        }
        
        private fun requestDeviceAdminPermission(activity: Activity) {
            if (isDeviceAdminEnabled(activity)) {
                currentStep++
                requestNextPermission(activity)
                return
            }
            
            showPermissionDialog(
                activity = activity,
                title = "🛡️ تفعيل صلاحية مشرف الجهاز",
                message = "هذه الصلاحية مطلوبة لحماية التطبيق من الحذف (اختياري)",
                action = {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, 
                        DeviceAdminReceiver.getComponentName(activity))
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
                        "هذه الصلاحية مطلوبة لحماية التطبيق من الحذف")
                    activity.startActivity(intent)
                },
                onNext = {
                    currentStep++
                    requestNextPermission(activity)
                },
                isOptional = true
            )
        }
        
        private fun requestNotificationPermission(activity: Activity) {
            if (isNotificationListenerEnabled(activity)) {
                currentStep++
                requestNextPermission(activity)
                return
            }
            
            showPermissionDialog(
                activity = activity,
                title = "🔔 تفعيل مراقبة الإشعارات",
                message = "هذه الصلاحية مطلوبة لمراقبة إشعارات التطبيقات",
                action = {
                    try {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        activity.startActivity(intent)
                        Toast.makeText(activity, "يرجى تفعيل مراقبة الإشعارات لـ Irada", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${activity.packageName}")
                        activity.startActivity(intent)
                    }
                },
                onNext = {
                    currentStep++
                    requestNextPermission(activity)
                }
            )
        }
        
        private fun showPermissionDialog(
            activity: Activity,
            title: String,
            message: String,
            action: () -> Unit,
            onNext: () -> Unit,
            isOptional: Boolean = false
        ) {
            // Show detailed instruction
            val instruction = if (isOptional) {
                "$title\n$message\n\nاضغط 'تخطي' لتجاهل هذه الصلاحية"
            } else {
                "$title\n$message\n\nيرجى تفعيل الصلاحية ثم العودة للتطبيق"
            }
            
            Toast.makeText(activity, instruction, Toast.LENGTH_LONG).show()
            
            // Execute the action
            action()
            
            // Show next step after a delay (shorter for better UX)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onNext()
            }, 1500)
        }
        
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            val expectedService = "${context.packageName}/${ContentBlockingService::class.java.name}"
            val isEnabled = enabledServices?.contains(expectedService) == true
            
            android.util.Log.d("Permission", "Accessibility check - Expected: $expectedService, Enabled: $enabledServices, Result: $isEnabled")
            return isEnabled
        }
        
        fun isSystemAlertWindowEnabled(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }
        
        fun isDeviceAdminEnabled(context: Context): Boolean {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return devicePolicyManager.isAdminActive(DeviceAdminReceiver.getComponentName(context))
        }
        
        fun isNotificationListenerEnabled(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return enabledListeners?.contains("${context.packageName}/${com.irada.blockerheroar.service.NotificationListenerService::class.java.name}") == true
        }
        
        fun checkAllPermissions(context: Context): Boolean {
            return isAccessibilityServiceEnabled(context) &&
                   isSystemAlertWindowEnabled(context) &&
                   isDeviceAdminEnabled(context) &&
                   isNotificationListenerEnabled(context)
        }
        
        fun getPermissionStatus(context: Context): String {
            val accessibility = if (isAccessibilityServiceEnabled(context)) "✅" else "❌"
            val overlay = if (isSystemAlertWindowEnabled(context)) "✅" else "❌"
            val deviceAdmin = if (isDeviceAdminEnabled(context)) "✅" else "❌"
            val notification = if (isNotificationListenerEnabled(context)) "✅" else "❌"
            
            return """
                إمكانية الوصول: $accessibility
                الظهور فوق التطبيقات: $overlay
                مشرف الجهاز: $deviceAdmin
                مراقبة الإشعارات: $notification
            """.trimIndent()
        }
        
        // Debug method to help troubleshoot permission issues
        fun debugPermissionStatus(context: Context): String {
            val accessibility = isAccessibilityServiceEnabled(context)
            val overlay = isSystemAlertWindowEnabled(context)
            val deviceAdmin = isDeviceAdminEnabled(context)
            val notification = isNotificationListenerEnabled(context)
            
            val debugInfo = StringBuilder()
            debugInfo.appendLine("=== Permission Debug Info ===")
            debugInfo.appendLine("Package: ${context.packageName}")
            debugInfo.appendLine("")
            
            // Accessibility Service
            debugInfo.appendLine("1. Accessibility Service:")
            debugInfo.appendLine("   Status: ${if (accessibility) "✅ ENABLED" else "❌ DISABLED"}")
            debugInfo.appendLine("   Expected: ${context.packageName}/${ContentBlockingService::class.java.name}")
            
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            debugInfo.appendLine("   Current enabled: $enabledServices")
            debugInfo.appendLine("")
            
            // System Alert Window
            debugInfo.appendLine("2. System Alert Window:")
            debugInfo.appendLine("   Status: ${if (overlay) "✅ ENABLED" else "❌ DISABLED"}")
            debugInfo.appendLine("   Can Draw Overlays: ${Settings.canDrawOverlays(context)}")
            debugInfo.appendLine("")
            
            // Device Admin
            debugInfo.appendLine("3. Device Admin:")
            debugInfo.appendLine("   Status: ${if (deviceAdmin) "✅ ENABLED" else "❌ DISABLED"}")
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = DeviceAdminReceiver.getComponentName(context)
            debugInfo.appendLine("   Component: $adminComponent")
            debugInfo.appendLine("   Is Admin Active: ${devicePolicyManager.isAdminActive(adminComponent)}")
            debugInfo.appendLine("")
            
            // Notification Listener
            debugInfo.appendLine("4. Notification Listener:")
            debugInfo.appendLine("   Status: ${if (notification) "✅ ENABLED" else "❌ DISABLED"}")
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            debugInfo.appendLine("   Current enabled: $enabledListeners")
            debugInfo.appendLine("   Expected: ${context.packageName}/${com.irada.blockerheroar.service.NotificationListenerService::class.java.name}")
            debugInfo.appendLine("")
            
            // Additional checks
            debugInfo.appendLine("=== Additional Checks ===")
            debugInfo.appendLine("Battery Optimization: ${isIgnoringBatteryOptimizations(context)}")
            debugInfo.appendLine("Auto Start: Check device settings manually")
            
            return debugInfo.toString()
        }

        private fun isIgnoringBatteryOptimizations(context: Context): String {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                    "✅ IGNORED"
                } else {
                    "❌ NOT IGNORED"
                }
            } else {
                "N/A (API < 23)"
            }
        }
    }
}