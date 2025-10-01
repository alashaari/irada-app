package com.irada.blockerheroar.utils

import android.content.Context
import android.content.SharedPreferences
import com.irada.blockerheroar.data.AppSettings
import com.irada.blockerheroar.data.User

object AppPreferences {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun getSharedPreferences(): SharedPreferences {
        if (!::sharedPreferences.isInitialized) {
            // Initialize with default context if not initialized
            if (::context.isInitialized) {
                sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            } else {
                throw IllegalStateException("AppPreferences not initialized. Call initialize() first.")
            }
        }
        return sharedPreferences
    }
    
    private const val PREFS_NAME = "irada_preferences"
    
    // Keys
    private const val KEY_USER_UID = "user_uid"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_SUBSCRIPTION_END_DATE = "subscription_end_date"
    private const val KEY_PARTNER_EMAIL = "partner_email"
    
    // Settings Keys
    private const val KEY_BLOCK_PORN = "block_porn"
    private const val KEY_BLOCK_YOUTUBE_SHORTS = "block_youtube_shorts"
    private const val KEY_BLOCK_SNAPCHAT = "block_snapchat"
    private const val KEY_BLOCK_INSTAGRAM = "block_instagram"
    private const val KEY_BLOCK_TELEGRAM = "block_telegram"
    private const val KEY_BLOCKING_MESSAGE = "blocking_message"
    private const val KEY_BLOCKING_DURATION = "blocking_duration"
    private const val KEY_PREVENT_DELETION = "prevent_deletion"
    private const val KEY_PROTECTION_DURATION = "protection_duration"
    private const val KEY_PROTECTION_START_DATE = "protection_start_date"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_BLOCK_NEW_APPS = "block_new_apps"
    private const val KEY_PREVENT_FACTORY_RESET = "prevent_factory_reset"
    private const val KEY_ACCESSIBILITY_ENABLED = "accessibility_enabled"
    private const val KEY_DEVICE_ADMIN_ENABLED = "device_admin_enabled"
    
    // User Preferences
    fun saveUser(user: User) {
        getSharedPreferences().edit().apply {
            putString(KEY_USER_UID, user.uid)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.name)
            putBoolean(KEY_IS_PREMIUM, user.isPremium)
            putLong(KEY_SUBSCRIPTION_END_DATE, user.subscriptionEndDate)
            putString(KEY_PARTNER_EMAIL, user.partnerEmail)
            apply()
        }
    }
    
    fun getUser(): User {
        return User(
            uid = getSharedPreferences().getString(KEY_USER_UID, "") ?: "",
            email = getSharedPreferences().getString(KEY_USER_EMAIL, "") ?: "",
            name = getSharedPreferences().getString(KEY_USER_NAME, "") ?: "",
            isPremium = getSharedPreferences().getBoolean(KEY_IS_PREMIUM, false),
            subscriptionEndDate = getSharedPreferences().getLong(KEY_SUBSCRIPTION_END_DATE, 0L),
            partnerEmail = getSharedPreferences().getString(KEY_PARTNER_EMAIL, "") ?: ""
        )
    }
    
    fun clearUser() {
        getSharedPreferences().edit().apply {
            remove(KEY_USER_UID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_IS_PREMIUM)
            remove(KEY_SUBSCRIPTION_END_DATE)
            remove(KEY_PARTNER_EMAIL)
            apply()
        }
    }
    
    // App Settings
    fun saveSettings(settings: AppSettings) {
        getSharedPreferences().edit().apply {
            putBoolean(KEY_BLOCK_PORN, settings.blockPornographicContent)
            putBoolean(KEY_BLOCK_YOUTUBE_SHORTS, settings.blockYouTubeShorts)
            putBoolean(KEY_BLOCK_SNAPCHAT, settings.blockSnapchatStories)
            putBoolean(KEY_BLOCK_INSTAGRAM, settings.blockInstagramExplore)
            putBoolean(KEY_BLOCK_TELEGRAM, settings.blockTelegramSearch)
            putString(KEY_BLOCKING_MESSAGE, settings.blockingMessage)
            putLong(KEY_BLOCKING_DURATION, settings.blockingDuration)
            putBoolean(KEY_PREVENT_DELETION, settings.preventAppDeletion)
            putInt(KEY_PROTECTION_DURATION, settings.protectionDuration)
            putLong(KEY_PROTECTION_START_DATE, settings.protectionStartDate)
            putBoolean(KEY_BLOCK_NEW_APPS, settings.blockNewApps)
            putBoolean(KEY_PREVENT_FACTORY_RESET, settings.preventFactoryReset)
            putBoolean(KEY_ACCESSIBILITY_ENABLED, settings.isAccessibilityServiceEnabled)
            putBoolean(KEY_DEVICE_ADMIN_ENABLED, settings.isDeviceAdminEnabled)
            apply()
        }
    }
    
    fun getSettings(): AppSettings {
        return AppSettings(
            blockPornographicContent = getSharedPreferences().getBoolean(KEY_BLOCK_PORN, false),
            blockYouTubeShorts = getSharedPreferences().getBoolean(KEY_BLOCK_YOUTUBE_SHORTS, false),
            blockSnapchatStories = getSharedPreferences().getBoolean(KEY_BLOCK_SNAPCHAT, false),
            blockInstagramExplore = getSharedPreferences().getBoolean(KEY_BLOCK_INSTAGRAM, false),
            blockTelegramSearch = getSharedPreferences().getBoolean(KEY_BLOCK_TELEGRAM, false),
            blockingMessage = getSharedPreferences().getString(KEY_BLOCKING_MESSAGE, "هذا المحتوى محظور") ?: "هذا المحتوى محظور",
            blockingDuration = getSharedPreferences().getLong(KEY_BLOCKING_DURATION, 5000L),
            preventAppDeletion = getSharedPreferences().getBoolean(KEY_PREVENT_DELETION, false),
            protectionDuration = getSharedPreferences().getInt(KEY_PROTECTION_DURATION, 30),
            protectionStartDate = getSharedPreferences().getLong(KEY_PROTECTION_START_DATE, 0L),
            blockNewApps = getSharedPreferences().getBoolean(KEY_BLOCK_NEW_APPS, false),
            preventFactoryReset = getSharedPreferences().getBoolean(KEY_PREVENT_FACTORY_RESET, false),
            isAccessibilityServiceEnabled = getSharedPreferences().getBoolean(KEY_ACCESSIBILITY_ENABLED, false),
            isDeviceAdminEnabled = getSharedPreferences().getBoolean(KEY_DEVICE_ADMIN_ENABLED, false)
        )
    }
    
    // Language
    fun setLanguage(language: String) {
        getSharedPreferences().edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    fun getLanguage(): String {
        return getSharedPreferences().getString(KEY_LANGUAGE, "ar") ?: "ar"
    }
    
    // Individual settings updates
    fun updateSetting(key: String, value: Boolean) {
        getSharedPreferences().edit().putBoolean(key, value).apply()
    }
    
    fun updateSetting(key: String, value: String) {
        getSharedPreferences().edit().putString(key, value).apply()
    }
    
    fun updateSetting(key: String, value: Long) {
        getSharedPreferences().edit().putLong(key, value).apply()
    }
    
    fun updateSetting(key: String, value: Int) {
        getSharedPreferences().edit().putInt(key, value).apply()
    }
    
    // Individual settings getters
    fun getSetting(key: String, defaultValue: String): String {
        return getSharedPreferences().getString(key, defaultValue) ?: defaultValue
    }
    
    fun getSetting(key: String, defaultValue: Boolean): Boolean {
        return getSharedPreferences().getBoolean(key, defaultValue)
    }
    
    fun getSetting(key: String, defaultValue: Long): Long {
        return getSharedPreferences().getLong(key, defaultValue)
    }
    
    fun getSetting(key: String, defaultValue: Int): Int {
        return getSharedPreferences().getInt(key, defaultValue)
    }
    
    
}