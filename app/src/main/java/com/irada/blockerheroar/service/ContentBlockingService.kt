package com.irada.blockerheroar.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.irada.blockerheroar.data.AppSettings
import com.irada.blockerheroar.utils.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper

class ContentBlockingService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var settings = AppSettings()
    private var lastBlockTime = 0L
    private val blockCooldown = 2000L // 2 seconds cooldown to prevent spam
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        loadSettings()
        android.util.Log.d("ContentBlockingService", "Service connected successfully")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { accessibilityEvent ->
            // Log for debugging
            android.util.Log.d("ContentBlockingService", 
                "Event: ${accessibilityEvent.eventType}, Package: ${accessibilityEvent.packageName}")
            
            when (accessibilityEvent.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleWindowStateChanged(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    handleViewClicked(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    handleViewScrolled(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    handleViewTextChanged(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    handleViewFocused(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    handleWindowContentChanged(accessibilityEvent)
                }
            }
        }
    }
    
    override fun onInterrupt() {
        android.util.Log.d("ContentBlockingService", "Service interrupted")
    }
    
    private fun loadSettings() {
        serviceScope.launch {
            settings = AppPreferences.getSettings()
            android.util.Log.d("ContentBlockingService", "Settings loaded: blockYouTubeShorts=${settings.blockYouTubeShorts}")
        }
    }
    
    // Enhanced window state change handler
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        
        if (packageName == null) return
        
        android.util.Log.d("ContentBlockingService", "Window changed - Package: $packageName, Class: $className")
        
        // Only check for YouTube Shorts when window changes, don't block immediately
        if (packageName == "com.google.android.youtube" && settings.blockYouTubeShorts) {
            // Just log that YouTube is opened, don't block yet
            android.util.Log.d("ContentBlockingService", "YouTube opened - monitoring for Shorts...")
            
            // Check after a delay to allow UI to load and user to navigate
            Handler(Looper.getMainLooper()).postDelayed({
                checkYouTubeShorts(event)
            }, 1000) // Increased delay to give user time to navigate
        }
        
        // Check if we should block this app (for other apps, not YouTube)
        if (shouldBlockApp(packageName)) {
            showBlockingScreenWithCooldown(packageName)
        }
    }
    
    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        
        if (packageName == "com.google.android.youtube" && settings.blockYouTubeShorts) {
            // Delay check to allow content to load
            Handler(Looper.getMainLooper()).postDelayed({
                checkYouTubeShorts(event)
            }, 300)
        }
    }
    
    private fun handleViewClicked(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        
        if (packageName == null) return
        
        // Check for specific content blocking
        when (packageName) {
            "com.google.android.youtube" -> {
                if (settings.blockYouTubeShorts) {
                    checkYouTubeShorts(event)
                }
            }
            "com.snapchat.android" -> {
                if (settings.blockSnapchatStories) {
                    checkSnapchatStories(event)
                }
            }
            "com.instagram.android" -> {
                if (settings.blockInstagramExplore) {
                    checkInstagramExplore(event)
                }
            }
            "org.telegram.messenger" -> {
                if (settings.blockTelegramSearch) {
                    checkTelegramSearch(event)
                }
            }
        }
    }
    
    private fun handleViewScrolled(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        
        if (packageName == "com.google.android.youtube" && settings.blockYouTubeShorts) {
            checkYouTubeShorts(event)
        }
    }
    
    private fun handleViewTextChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        
        if (packageName == "com.google.android.youtube" && settings.blockYouTubeShorts) {
            checkYouTubeShorts(event)
        }
    }
    
    private fun handleViewFocused(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        
        if (packageName == "com.google.android.youtube" && settings.blockYouTubeShorts) {
            checkYouTubeShorts(event)
        }
    }
    
    private fun shouldBlockApp(packageName: String): Boolean {
        // Check if this app is in the blocked list
        // This would be implemented based on the blocked apps list
        return false // Placeholder
    }
    
    // Enhanced YouTube Shorts detection
    private fun checkYouTubeShorts(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        if (packageName != "com.google.android.youtube") return
        
        android.util.Log.d("ContentBlockingService", "Checking YouTube Shorts...")
        
        // Only check if user is actually in Shorts section
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            // Check for Shorts-specific indicators
            val isInShorts = checkForShortsIndicators(rootNode)
            
            if (isInShorts) {
                android.util.Log.d("ContentBlockingService", "YouTube Shorts detected! Blocking...")
                showBlockingScreenWithCooldown("com.google.android.youtube")
                return
            }
        }
        
        // Fallback: check event source
        val source = event.source
        if (source != null && checkNodeForShortsContent(source)) {
            android.util.Log.d("ContentBlockingService", "YouTube Shorts content detected! Blocking...")
            showBlockingScreenWithCooldown("com.google.android.youtube")
        }
    }
    
    // Check for specific Shorts indicators
    private fun checkForShortsIndicators(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDescription = node.contentDescription?.toString()?.lowercase() ?: ""
        val className = node.className?.toString()?.lowercase() ?: ""
        
        // Check for Shorts-specific keywords
        val shortsKeywords = listOf(
            "#shorts", "#قصير", "shorts", "قصير", "قصيرة",
            "youtube shorts", "يوتيوب قصير", "shorts player", "مشغل القصير"
        )
        
        val hasShortsKeyword = shortsKeywords.any { keyword ->
            text.contains(keyword) || contentDescription.contains(keyword)
        }
        
        // Check for Shorts URL patterns
        val hasShortsUrl = text.contains("/shorts/") || 
                          contentDescription.contains("/shorts/") ||
                          text.contains("youtube.com/shorts") ||
                          contentDescription.contains("youtube.com/shorts")
        
        // Check for Shorts UI elements
        val hasShortsUI = className.contains("shorts") || 
                         text.contains("shorts") ||
                         contentDescription.contains("shorts")
        
        // Only return true if we're very confident it's Shorts
        return hasShortsUrl || (hasShortsKeyword && hasShortsUI)
    }

    private fun checkForShortsUrl(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        
        // Check for Shorts URL patterns
        val shortsUrlPatterns = listOf(
            "/shorts/",
            "youtube.com/shorts",
            "youtu.be/shorts",
            "m.youtube.com/shorts"
        )
        
        val hasUrlPattern = shortsUrlPatterns.any { pattern ->
            text.contains(pattern) || contentDesc.contains(pattern)
        }
        
        if (hasUrlPattern) return true
        
        // Check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (checkForShortsUrl(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }
        
        return false
    }

    private fun checkForShortsUIElements(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString()?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val resourceId = node.viewIdResourceName?.lowercase() ?: ""
        
        // Check for specific Shorts UI indicators
        val shortsUIIndicators = listOf(
            // English indicators
            "shorts", "short video", "shorts feed", "shorts player",
            "vertical video", "swipe up", "swipe down", "shorts tab",
            // Arabic indicators
            "قصير", "فيديو قصير", "مقاطع قصيرة", "مشغل المقاطع القصيرة"
        )
        
        // Check for Shorts-specific resource IDs
        val shortsResourcePatterns = listOf(
            "shorts", "reel", "vertical", "short_video"
        )
        
        val hasShortsResourceId = shortsResourcePatterns.any { pattern ->
            resourceId.contains(pattern)
        }
        
        // Check for Shorts-specific class names
        val isShortsClass = className.contains("shorts") || 
                           className.contains("reel") ||
                           className.contains("vertical")
        
        // Check for Shorts text/description
        val hasShortsText = shortsUIIndicators.any { indicator ->
            text.contains(indicator) || contentDesc.contains(indicator)
        }
        
        // Check for #shorts hashtag
        val hasShortsHashtag = text.contains("#shorts") || 
                              contentDesc.contains("#shorts") ||
                              text.contains("#قصير") ||
                              contentDesc.contains("#قصير")
        
        if (hasShortsResourceId || (isShortsClass && hasShortsText) || hasShortsHashtag) {
            android.util.Log.d("ContentBlockingService", "Shorts UI found - Resource: $hasShortsResourceId, Class: $isShortsClass, Text: $hasShortsText, Hashtag: $hasShortsHashtag")
            return true
        }
        
        // Check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (checkForShortsUIElements(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }
        
        return false
    }

    private fun checkNodeForShortsContent(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val resourceId = node.viewIdResourceName?.lowercase() ?: ""
        
        // Check for specific resource IDs related to Shorts
        val shortsResourceIds = listOf(
            "shorts", "reel", "vertical_video", "short_video"
        )
        
        val hasShortsResourceId = shortsResourceIds.any { id ->
            resourceId.contains(id)
        }
        
        // Check for Shorts content patterns
        val shortsContentPatterns = listOf(
            "#shorts", "#قصير", "youtube shorts", "يوتيوب قصير",
            "shorts player", "مشغل القصير"
        )
        
        val hasShortsContent = shortsContentPatterns.any { pattern ->
            text.contains(pattern) || contentDesc.contains(pattern)
        }
        
        return hasShortsResourceId || hasShortsContent
    }
    
    private fun checkSnapchatStories(event: AccessibilityEvent) {
        val source = event.source
        if (source != null) {
            val text = source.text?.toString()?.lowercase()
            if (text?.contains("story") == true || text?.contains("قصة") == true) {
                showBlockingScreenWithCooldown("com.snapchat.android")
            }
        }
    }
    
    private fun checkInstagramExplore(event: AccessibilityEvent) {
        val source = event.source
        if (source != null) {
            val text = source.text?.toString()?.lowercase()
            if (text?.contains("explore") == true || text?.contains("استكشاف") == true) {
                showBlockingScreenWithCooldown("com.instagram.android")
            }
        }
    }
    
    private fun checkTelegramSearch(event: AccessibilityEvent) {
        val source = event.source
        if (source != null) {
            val text = source.text?.toString()?.lowercase()
            if (text?.contains("search") == true || text?.contains("بحث") == true) {
                showBlockingScreenWithCooldown("org.telegram.messenger")
            }
        }
    }
    
    private fun showBlockingScreenWithCooldown(packageName: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlockTime < blockCooldown) {
            android.util.Log.d("ContentBlockingService", "Blocking on cooldown, skipping...")
            return
        }
        
        lastBlockTime = currentTime
        showBlockingScreen(packageName)
    }
    
    private fun showBlockingScreen(packageName: String) {
        android.util.Log.d("ContentBlockingService", "Showing blocking screen for: $packageName")
        
        val intent = Intent(this, com.irada.blockerheroar.ui.BlockingScreenActivity::class.java).apply {
            putExtra("package_name", packageName)
            putExtra("blocking_message", settings.blockingMessage)
            putExtra("blocking_duration", settings.blockingDuration)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
    
    private fun checkPornographicContent(event: AccessibilityEvent) {
        if (!settings.blockPornographicContent) return
        
        val source = event.source
        if (source != null) {
            val text = source.text?.toString()?.lowercase()
            val pornographicKeywords = listOf(
                "porn", "xxx", "sex", "adult", "nude", "naked",
                "إباحي", "جنس", "عري", "محتوى للبالغين"
            )
            
            if (pornographicKeywords.any { text?.contains(it) == true }) {
                showBlockingScreenWithCooldown(event.packageName?.toString() ?: "")
            }
        }
    }
    
    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        android.util.Log.d("ContentBlockingService", "Settings updated: blockYouTubeShorts=${settings.blockYouTubeShorts}")
    }
}