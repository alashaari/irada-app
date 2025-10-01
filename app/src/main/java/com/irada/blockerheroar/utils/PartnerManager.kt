package com.irada.blockerheroar.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class PartnerRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromUserEmail: String = "",
    val toUserEmail: String = "",
    val featureName: String = "",
    val requestType: String = "",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)

object PartnerManager {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private const val EMAILJS_SERVICE_ID = "service_3n9xri9"
    private const val EMAILJS_TEMPLATE_ID = "template_vhnznme"
    private const val EMAILJS_PUBLIC_KEY = "1i4TjwaVGz2HQXcBe"
    private const val EMAILJS_PRIVATE_KEY = "7GBHimZSVAs5mpSOVeP-Z"  // ضع Private Key هنا
    
    private fun getUserId(): String? = auth.currentUser?.uid
    private fun getUserEmail(): String? = auth.currentUser?.email
    
    suspend fun addPartner(partnerEmail: String): Boolean {
        android.util.Log.e("IRADA_PARTNER", "========== addPartner CALLED ==========")
        android.util.Log.e("IRADA_PARTNER", "Partner Email: $partnerEmail")
        
        return try {
            val userId = getUserId()
            android.util.Log.e("IRADA_PARTNER", "User ID: $userId")
            
            if (userId == null) {
                android.util.Log.e("IRADA_PARTNER", "User ID is NULL!")
                return false
            }
            
            val userEmail = getUserEmail()
            android.util.Log.e("IRADA_PARTNER", "User Email: $userEmail")
            
            if (userEmail == null) {
                android.util.Log.e("IRADA_PARTNER", "User Email is NULL!")
                return false
            }
            
            android.util.Log.e("IRADA_PARTNER", "Saving to Firestore...")
            
            // إنشاء PartnerRequest أولاً
            val partnerRequest = mapOf(
                "fromUserEmail" to userEmail,
                "toUserEmail" to partnerEmail,
                "featureName" to "إضافة شريك",
                "requestType" to "add",
                "status" to "pending",
                "timestamp" to System.currentTimeMillis()
            )
            
            val docRef = firestore.collection("partner_requests").add(partnerRequest).await()
            val requestId = docRef.id
            
            android.util.Log.e("IRADA_PARTNER", "Partner request created with ID: $requestId")
            
            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "email" to userEmail,
                        "partnerEmail" to partnerEmail,
                        "userId" to userId,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            
            android.util.Log.e("IRADA_PARTNER", "Firestore save SUCCESS!")
            
            AppPreferences.updateSetting("partner_email", partnerEmail as String)
            android.util.Log.e("IRADA_PARTNER", "AppPreferences updated!")
            
            android.util.Log.e("IRADA_PARTNER", "Sending invitation email...")
            sendPartnerInvitation(userEmail, partnerEmail, requestId)
            
            android.util.Log.e("IRADA_PARTNER", "addPartner completed successfully!")
            true
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "ERROR in addPartner: ${e.message}", e)
            false
        }
    }
    
    suspend fun removePartner(): Boolean {
        android.util.Log.e("IRADA_PARTNER", "========== removePartner CALLED ==========")
        return try {
            val userId = getUserId() ?: return false
            
            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf("partnerEmail" to ""),
                    SetOptions.merge()
                )
                .await()
            
            AppPreferences.updateSetting("partner_email", "" as String)
            android.util.Log.e("IRADA_PARTNER", "removePartner SUCCESS!")
            true
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "ERROR in removePartner: ${e.message}")
            false
        }
    }
    
    suspend fun getPartnerEmail(): String {
        return try {
            val userId = getUserId() ?: return ""
            
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            doc.getString("partnerEmail") ?: ""
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "Error getting partner: ${e.message}")
            ""
        }
    }
    
    suspend fun sendApprovalRequest(featureName: String, requestType: String): Boolean {
        android.util.Log.e("IRADA_PARTNER", "========== sendApprovalRequest CALLED ==========")
        android.util.Log.e("IRADA_PARTNER", "Feature: $featureName, Type: $requestType")
        
        return try {
            val userId = getUserId() ?: return false
            val userEmail = getUserEmail() ?: return false
            val partnerEmail = getPartnerEmail()
            
            android.util.Log.e("IRADA_PARTNER", "Partner Email: $partnerEmail")
            
            if (partnerEmail.isEmpty()) {
                android.util.Log.e("IRADA_PARTNER", "Partner email is EMPTY!")
                return false
            }
            
            val request = PartnerRequest(
                id = System.currentTimeMillis().toString(),
                fromUserId = userId,
                fromUserEmail = userEmail,
                toUserEmail = partnerEmail,
                featureName = featureName,
                requestType = requestType,
                status = "pending"
            )
            
            android.util.Log.e("IRADA_PARTNER", "Saving request to Firestore...")
            
            firestore.collection("partner_requests")
                .document(request.id)
                .set(request)
                .await()
            
            android.util.Log.e("IRADA_PARTNER", "Request saved! Sending email...")
            
            sendEmailToPartner(request)
            
            android.util.Log.e("IRADA_PARTNER", "sendApprovalRequest completed!")
            true
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "ERROR in sendApprovalRequest: ${e.message}", e)
            false
        }
    }
    
    suspend fun approveRequest(requestId: String): Boolean {
        return try {
            firestore.collection("partner_requests")
                .document(requestId)
                .update("status", "approved")
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "Error approving request: ${e.message}")
            false
        }
    }
    
    suspend fun rejectRequest(requestId: String): Boolean {
        return try {
            firestore.collection("partner_requests")
                .document(requestId)
                .update("status", "rejected")
                .await()
            true
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "Error rejecting request: ${e.message}")
            false
        }
    }
    
    suspend fun getPendingRequests(): List<PartnerRequest> {
        return try {
            val userEmail = getUserEmail() ?: return emptyList()
            
            val snapshot = firestore.collection("partner_requests")
                .whereEqualTo("toUserEmail", userEmail)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PartnerRequest::class.java)
            }
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "Error getting requests: ${e.message}")
            emptyList()
        }
    }
    
    private suspend fun sendPartnerInvitation(fromEmail: String, toEmail: String, requestId: String) {
        android.util.Log.e("IRADA_PARTNER", "========== sendPartnerInvitation CALLED ==========")
        android.util.Log.e("IRADA_PARTNER", "From: $fromEmail, To: $toEmail")
        
        try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                
                val jsonObject = JSONObject().apply {
                    put("service_id", EMAILJS_SERVICE_ID)
                    put("template_id", EMAILJS_TEMPLATE_ID)
                    put("user_id", EMAILJS_PUBLIC_KEY)
                    put("accessToken", EMAILJS_PRIVATE_KEY) // إضافة Private Key هنا
                    put("template_params", JSONObject().apply {
                        put("to_email", toEmail)
                        put("from_email", fromEmail)
                        put("feature_name", "إضافة شريك")
                        put("action", "دعوة")
                        put("message", "تمت إضافتك كشريك في تطبيق إرادة من قبل $fromEmail")
                        put("approve_url", "https://darkslateblue-manatee-671938.hostingersite.com/approve/?requestId=${requestId}&action=approve")
                        put("reject_url", "https://darkslateblue-manatee-671938.hostingersite.com/reject/?requestId=${requestId}&action=reject")
                    })
                }
                
                android.util.Log.e("IRADA_PARTNER", "JSON prepared: ${jsonObject.toString()}")
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonObject.toString().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                
                android.util.Log.e("IRADA_PARTNER", "Sending HTTP request to EmailJS...")
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                android.util.Log.e("IRADA_PARTNER", "Response Code: ${response.code}")
                android.util.Log.e("IRADA_PARTNER", "Response Message: ${response.message}")
                android.util.Log.e("IRADA_PARTNER", "Response Body: $responseBody")
                
                if (response.isSuccessful) {
                    android.util.Log.e("IRADA_PARTNER", "✅ Email sent SUCCESSFULLY!")
                } else {
                    android.util.Log.e("IRADA_PARTNER", "❌ Email FAILED to send!")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "❌ EXCEPTION in sendPartnerInvitation: ${e.message}", e)
        }
    }
    
    private suspend fun sendEmailToPartner(request: PartnerRequest) {
        android.util.Log.e("IRADA_PARTNER", "========== sendEmailToPartner CALLED ==========")
        
        try {
            val approveUrl = "https://darkslateblue-manatee-671938.hostingersite.com/approve-feature/?requestId=${request.id}&action=approve"
            val rejectUrl = "https://darkslateblue-manatee-671938.hostingersite.com/reject-feature/?requestId=${request.id}&action=reject"
            
            val featureNameArabic = when(request.featureName) {
                "block_youtube_shorts" -> "حظر مقاطع يوتيوب القصيرة"
                "block_snapchat_stories" -> "حظر قصص سناب شات"
                "block_instagram_explore" -> "حظر استكشاف انستقرام"
                "block_telegram_search" -> "حظر بحث تيليجرام"
                "block_pornographic_content" -> "حظر المحتوى الإباحي"
                else -> request.featureName
            }
            
            val actionArabic = if (request.requestType == "disable") "تعطيل" else "تفعيل"
            
            android.util.Log.e("IRADA_PARTNER", "Feature: $featureNameArabic, Action: $actionArabic")
            
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                
                val jsonObject = JSONObject().apply {
                    put("service_id", EMAILJS_SERVICE_ID)
                    put("template_id", EMAILJS_TEMPLATE_ID)
                    put("user_id", EMAILJS_PUBLIC_KEY)
                    put("accessToken", EMAILJS_PRIVATE_KEY) // إضافة Private Key هنا
                    put("template_params", JSONObject().apply {
                        put("to_email", request.toUserEmail)
                        put("from_email", request.fromUserEmail)
                        put("feature_name", featureNameArabic)
                        put("action", actionArabic)
                        put("message", "يرجى مراجعة هذا الطلب والموافقة عليه أو رفضه")
                        put("approve_url", approveUrl)
                        put("reject_url", rejectUrl)
                    })
                }
                
                android.util.Log.e("IRADA_PARTNER", "Sending request email...")
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonObject.toString().toRequestBody(mediaType)
                
                val httpRequest = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                val responseBody = response.body?.string()
                
                android.util.Log.e("IRADA_PARTNER", "Response Code: ${response.code}")
                android.util.Log.e("IRADA_PARTNER", "Response Body: $responseBody")
                
                if (response.isSuccessful) {
                    android.util.Log.e("IRADA_PARTNER", "✅ Request email sent SUCCESSFULLY!")
                } else {
                    android.util.Log.e("IRADA_PARTNER", "❌ Request email FAILED!")
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "❌ EXCEPTION in sendEmailToPartner: ${e.message}", e)
        }
    }
    
    // دالة للتحقق من تحديثات الشراكة وإظهار رسائل
    suspend fun checkPartnerUpdates(): String? {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) return null
            
            val userEmail = currentUser.email ?: return null
            
            val snapshot = FirebaseFirestore.getInstance()
                .collection("partner_requests")
                .whereEqualTo("fromUserEmail", userEmail)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) return null
            
            val latestRequest = snapshot.documents.first()
            val lastCheckTime = AppPreferences.getSetting("last_partner_check", 0L)
            val requestTimestamp = latestRequest.getLong("timestamp") ?: 0L
            
            if (requestTimestamp > lastCheckTime) {
                // تحديث وقت آخر فحص
                AppPreferences.updateSetting("last_partner_check", requestTimestamp as Long)
                
                val status = latestRequest.getString("status") ?: "pending"
                val featureName = latestRequest.getString("featureName") ?: ""
                val requestType = latestRequest.getString("requestType") ?: ""
                val toUserEmail = latestRequest.getString("toUserEmail") ?: ""
                
                // إرجاع الرسالة المناسبة
                when {
                    status == "approved" && featureName == "إضافة شريك" -> {
                        "✅ وافق ${toUserEmail} على الشراكة!"
                    }
                    status == "rejected" && featureName == "إضافة شريك" -> {
                        "❌ رفض ${toUserEmail} الشراكة"
                    }
                    status == "approved" && requestType == "disable" -> {
                        "✅ وافق الشريك على إلغاء ${getFeatureNameArabic(featureName)}"
                    }
                    status == "rejected" && requestType == "disable" -> {
                        "❌ رفض الشريك إلغاء ${getFeatureNameArabic(featureName)}"
                    }
                    else -> null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("IRADA_PARTNER", "Error checking partner updates: ${e.message}")
            null
        }
    }
    
    // دالة مساعدة للحصول على اسم الميزة بالعربية
    private fun getFeatureNameArabic(featureName: String): String {
        return when(featureName) {
            "block_youtube_shorts" -> "حظر يوتيوب القصيرة"
            "block_snapchat_stories" -> "حظر سناب شات"
            "block_instagram_explore" -> "حظر انستقرام"
            "block_telegram_search" -> "حظر تيليجرام"
            "block_pornographic_content" -> "حظر المحتوى الإباحي"
            else -> featureName
        }
    }
}