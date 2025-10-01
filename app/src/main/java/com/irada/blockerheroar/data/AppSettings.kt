package com.irada.blockerheroar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppSettings(
    // Content Blocking Settings
    val blockPornographicContent: Boolean = false,
    val blockYouTubeShorts: Boolean = false,
    val blockSnapchatStories: Boolean = false,
    val blockInstagramExplore: Boolean = false,
    val blockTelegramSearch: Boolean = false,
    
    // Blocking Screen Settings
    val blockingMessage: String = "هذا المحتوى محظور",
    val blockingDuration: Long = 5000L, // 5 seconds
    
    // App Protection Settings
    val preventAppDeletion: Boolean = false,
    val protectionDuration: Int = 30, // days
    val protectionStartDate: Long = 0L,
    
    // Advanced Features
    val blockNewApps: Boolean = false,
    val preventFactoryReset: Boolean = false,
    
    // Partner Settings
    val partnerEmail: String = "",
    
    // Accessibility Service
    val isAccessibilityServiceEnabled: Boolean = false,
    
    // Device Admin
    val isDeviceAdminEnabled: Boolean = false
) : Parcelable
