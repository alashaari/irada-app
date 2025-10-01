package com.irada.blockerheroar.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.irada.blockerheroar.R

class BlockingOverlayService : Service() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("message") ?: "هذا المحتوى محظور"
        val duration = intent?.getLongExtra("duration", 5000L) ?: 5000L
        
        showOverlay(message)
        
        // Auto-hide after duration
        android.os.Handler(mainLooper).postDelayed({
            hideOverlay()
        }, duration)
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun showOverlay(message: String) {
        if (overlayView != null) return
        
        overlayView = TextView(this).apply {
            text = message
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@BlockingOverlayService, R.color.blocking_text))
            setBackgroundColor(ContextCompat.getColor(this@BlockingOverlayService, R.color.blocking_background))
            setPadding(32, 32, 32, 32)
        }
        
        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        
        windowManager?.addView(overlayView, layoutParams)
    }
    
    private fun hideOverlay() {
        overlayView?.let { view ->
            windowManager?.removeView(view)
            overlayView = null
        }
        stopSelf()
    }
}
