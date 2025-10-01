package com.irada.blockerheroar.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.irada.blockerheroar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdminManager {
    
    // قائمة الإيميلات المسموحة للمطورين
    private val adminEmails = listOf(
        "admin@irada.com",           // إيميلك الرئيسي
        "developer@irada.com",       // إيميل المطور
        "owner@irada.com"            // إيميل المالك
    )
    
    // إيميل المطور الحالي
    private var currentAdminEmail: String? = null
    
    fun setAdminEmail(email: String) {
        currentAdminEmail = email
    }
    
    fun isAdmin(): Boolean {
        return currentAdminEmail?.let { email ->
            adminEmails.contains(email.lowercase())
        } ?: false
    }
    
    fun isAdmin(email: String): Boolean {
        return adminEmails.contains(email.lowercase())
    }
    
    fun hasUnlimitedAccess(): Boolean {
        return isAdmin()
    }
    
    fun canAccessPremiumFeature(featureName: String): Boolean {
        return isAdmin() || AppPreferences.getUser().isPremium
    }
    
    // النسخة القديمة بدون ترجمة (للتوافق مع الكود القديم)
    fun getAdminStatus(): String {
        return when {
            isAdmin() -> "مطور - وصول كامل"
            AppPreferences.getUser().isPremium -> "مشترك - وصول كامل"
            else -> "مجاني - وصول محدود"
        }
    }
    
    // النسخة الجديدة مع الترجمة
    fun getAdminStatus(context: Context): String {
        return when {
            isAdmin() -> context.getString(R.string.subscription_developer)
            AppPreferences.getUser().isPremium -> context.getString(R.string.subscription_premium)
            else -> context.getString(R.string.subscription_free)
        }
    }
    
    fun addAdminEmail(email: String) {
        // يمكن إضافة إيميلات جديدة للمطورين
        // هذا يتطلب تحديث قاعدة البيانات
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("admin_emails")
                    .document(email.lowercase())
                    .set(mapOf(
                        "email" to email.lowercase(),
                        "addedAt" to System.currentTimeMillis(),
                        "isActive" to true
                    ))
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun removeAdminEmail(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("admin_emails")
                    .document(email.lowercase())
                    .delete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun loadAdminEmailsFromFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("admin_emails")
                    .get()
                    .addOnSuccessListener { documents ->
                        val emails = documents.mapNotNull { doc ->
                            if (doc.getBoolean("isActive") == true) {
                                doc.getString("email")
                            } else null
                        }
                        // تحديث قائمة الإيميلات
                        // يمكن إضافة منطق لتحديث adminEmails
                    }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}