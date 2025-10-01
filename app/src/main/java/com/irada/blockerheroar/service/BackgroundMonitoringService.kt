package com.irada.blockerheroar.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.irada.blockerheroar.R
import com.irada.blockerheroar.ui.MainActivity
import com.irada.blockerheroar.utils.AppPreferences

class BackgroundMonitoringService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "background_monitoring_channel"
        private const val CHANNEL_NAME = "مراقبة الخلفية"
        
        fun startService(context: Context) {
            val intent = Intent(context, BackgroundMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, BackgroundMonitoringService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("BackgroundService", "Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d("BackgroundService", "Service started")
        
        // Start monitoring logic here
        startMonitoring()
        
        return START_STICKY // Restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("BackgroundService", "Service destroyed")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "مراقبة التطبيقات في الخلفية"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Irada - مراقبة نشطة")
            .setContentText("التطبيق يراقب اليوتيوب في الخلفية")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun startMonitoring() {
        // Check if YouTube blocking is enabled
        try {
            val settings = AppPreferences.getSettings()
            if (settings.blockYouTubeShorts) {
                Log.d("BackgroundService", "YouTube blocking is enabled - monitoring active")
                // Here you would implement the actual monitoring logic
                // For now, we just log that monitoring is active
            } else {
                Log.d("BackgroundService", "YouTube blocking is disabled - stopping service")
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e("BackgroundService", "Error checking settings", e)
            stopSelf()
        }
    }
}


