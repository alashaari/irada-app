package com.irada.blockerheroar.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.irada.blockerheroar.R

class BlockingScreenActivity : AppCompatActivity() {
    
    private lateinit var blockingMessage: TextView
    private lateinit var exitButton: Button
    private var isExiting = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make activity fullscreen and prevent dismissal
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        
        setContentView(R.layout.activity_blocking_screen)
        
        blockingMessage = findViewById(R.id.blocking_message)
        exitButton = findViewById(R.id.exit_button)
        
        setupBlockingScreen()
    }
    
    private fun setupBlockingScreen() {
        val message = intent.getStringExtra("blocking_message") ?: "تم حظر هذا المحتوى"
        
        blockingMessage.text = message
        
        // Setup exit button
        exitButton.setOnClickListener {
            exitApp()
        }
    }
    
    private fun exitApp() {
        if (isExiting) return // Prevent multiple calls
        isExiting = true
        
        try {
            // Close YouTube app first
            closeYouTubeApp()
            
            // Go to home screen
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(homeIntent)
            
            // Finish this activity
            finish()
            
            // Force close all activities
            finishAffinity()
            
        } catch (e: Exception) {
            // Fallback: just finish this activity
            finish()
        }
    }
    
    private fun closeYouTubeApp() {
        try {
            // Try to close YouTube app using ActivityManager
            val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningTasks = activityManager.getRunningTasks(10)
            
            for (taskInfo in runningTasks) {
                if (taskInfo.topActivity?.packageName == "com.google.android.youtube") {
                    // Kill YouTube process
                    android.os.Process.killProcess(taskInfo.id)
                    break
                }
            }
        } catch (e: Exception) {
            // If killing process fails, try to minimize YouTube
            val minimizeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(minimizeIntent)
        }
    }
    
    override fun onBackPressed() {
        // Do nothing - prevent back button
    }
    
    override fun onUserLeaveHint() {
        // Do nothing - prevent leaving
    }
    
    override fun onPause() {
        super.onPause()
        // Only bring back to front if user is not exiting
        if (!isExiting) {
            val intent = Intent(this, BlockingScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}