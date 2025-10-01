package com.irada.blockerheroar.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import com.irada.blockerheroar.IradaApplication
import com.irada.blockerheroar.ui.MainActivity
import java.util.Locale

object LanguageManager {
    
    fun changeLanguage(context: Context, languageCode: String, restartActivity: Boolean = true) {
        // Save language preference
        AppPreferences.setLanguage(languageCode)
        
        // Apply language to application
        val app = context.applicationContext as IradaApplication
        app.setAppLanguage(languageCode)
        
        // Restart app from MainActivity if requested
        if (restartActivity && context is Activity) {
            restartApp(context)
        }
    }
    
    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    // إعادة تشغيل التطبيق بالكامل من MainActivity
    fun restartApp(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    fun getCurrentLanguage(): String {
        return AppPreferences.getLanguage()
    }
    
    fun isRTL(context: Context): Boolean {
        val currentLanguage = getCurrentLanguage()
        return currentLanguage == "ar"
    }
}