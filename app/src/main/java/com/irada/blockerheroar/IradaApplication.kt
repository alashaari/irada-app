package com.irada.blockerheroar

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.irada.blockerheroar.utils.AppPreferences
import java.util.Locale

class IradaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize app preferences FIRST
        AppPreferences.initialize(this)
        
        // Set up language support
        setupLanguage()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase services
        FirebaseAuth.getInstance()
        FirebaseFirestore.getInstance()
        
        // Initialize billing manager
        com.irada.blockerheroar.billing.BillingManager.initialize(this)
    }
    
    private fun setupLanguage() {
        try {
            // Get saved language preference
            val savedLanguage = AppPreferences.getLanguage()
            if (savedLanguage.isNotEmpty()) {
                setAppLanguage(savedLanguage)
            }
        } catch (e: Exception) {
            // If language setup fails, continue with default language
            android.util.Log.e("IradaApplication", "Language setup failed: ${e.message}")
        }
    }
    
    fun setAppLanguage(languageCode: String) {
        try {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            
            // CRITICAL FIX: Apply configuration to resources
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            
            // Save language preference
            AppPreferences.setLanguage(languageCode)
            
            android.util.Log.d("IradaApplication", "Language changed to: $languageCode")
        } catch (e: Exception) {
            android.util.Log.e("IradaApplication", "Language change failed: ${e.message}")
        }
    }
    
    override fun attachBaseContext(base: Context?) {
        val context = base?.let { updateLanguage(it) } ?: base
        super.attachBaseContext(context)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupLanguage()
    }
    
    companion object {
        fun updateLanguage(context: Context): Context {
            return try {
                AppPreferences.initialize(context)
                val savedLanguage = AppPreferences.getLanguage()
                
                if (savedLanguage.isNotEmpty()) {
                    val locale = Locale(savedLanguage)
                    val config = Configuration(context.resources.configuration)
                    config.setLocale(locale)
                    context.createConfigurationContext(config)
                } else {
                    context
                }
            } catch (e: Exception) {
                android.util.Log.e("IradaApplication", "updateLanguage failed: ${e.message}")
                context
            }
        }
    }
}