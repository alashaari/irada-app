package com.irada.blockerheroar.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.irada.blockerheroar.utils.AppPreferences

class NotificationListenerService : NotificationListenerService() {
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { notification ->
            val packageName = notification.packageName
            val notificationText = notification.notification.extras.getCharSequence("android.text")?.toString() ?: ""
            val notificationTitle = notification.notification.extras.getCharSequence("android.title")?.toString() ?: ""
            
            Log.d("NotificationListener", "Notification from $packageName: $notificationTitle - $notificationText")
            
            // Check if this is from a blocked app
            when (packageName) {
                "com.google.android.youtube" -> {
                    if (AppPreferences.getSettings().blockYouTubeShorts) {
                        checkYouTubeNotification(notificationText, notificationTitle)
                    }
                }
                "com.snapchat.android" -> {
                    if (AppPreferences.getSettings().blockSnapchatStories) {
                        checkSnapchatNotification(notificationText, notificationTitle)
                    }
                }
                "com.instagram.android" -> {
                    if (AppPreferences.getSettings().blockInstagramExplore) {
                        checkInstagramNotification(notificationText, notificationTitle)
                    }
                }
            }
        }
    }
    
    private fun checkYouTubeNotification(text: String, title: String) {
        val content = "$title $text".lowercase()
        
        val shortsKeywords = listOf("shorts", "قصير", "قصيرة", "short", "reel", "ريلز")
        
        if (shortsKeywords.any { content.contains(it) }) {
            Log.d("NotificationListener", "YouTube Shorts notification detected!")
            // Could show blocking screen or take action
        }
    }
    
    private fun checkSnapchatNotification(text: String, title: String) {
        val content = "$title $text".lowercase()
        
        val storyKeywords = listOf("story", "قصة", "stories", "قصص")
        
        if (storyKeywords.any { content.contains(it) }) {
            Log.d("NotificationListener", "Snapchat Story notification detected!")
            // Could show blocking screen or take action
        }
    }
    
    private fun checkInstagramNotification(text: String, title: String) {
        val content = "$title $text".lowercase()
        
        val exploreKeywords = listOf("explore", "استكشاف", "discover", "اكتشف")
        
        if (exploreKeywords.any { content.contains(it) }) {
            Log.d("NotificationListener", "Instagram Explore notification detected!")
            // Could show blocking screen or take action
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Handle notification removal if needed
    }
}


