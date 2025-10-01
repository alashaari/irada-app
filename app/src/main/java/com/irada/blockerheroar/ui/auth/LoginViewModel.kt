package com.irada.blockerheroar.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.irada.blockerheroar.utils.AdminManager
import com.irada.blockerheroar.data.User
import com.irada.blockerheroar.utils.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // EmailJS Configuration - محسن
    private val emailjsServiceId = "service_3n9xri9"
    private val emailjsTemplateId = "template_vhnznme"
    private val emailjsPublicKey = "1i4TjwaVGz2HQXcBe"
    private val emailjsPrivateKey = "7GBHimZSVAs5mpSOVeP-Z"
    
    // دالة محسنة لإرسال إيميل التحقق عبر EmailJS
    private suspend fun sendVerificationEmailViaEmailJS(userEmail: String, userName: String, verificationLink: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val htmlContent = """
                    <!DOCTYPE html>
                    <html dir="rtl" lang="ar">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>تفعيل الحساب</title>
                        <style>
                            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                            .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                            .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
                            .content { padding: 30px; }
                            .button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; margin: 20px 0; }
                            .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>مرحباً $userName</h1>
                                <p>أهلاً بك في تطبيق إرادة</p>
                            </div>
                            <div class="content">
                                <h2>تفعيل حسابك</h2>
                                <p>شكراً لك على التسجيل في تطبيق إرادة. لتفعيل حسابك والاستمتاع بجميع المميزات، اضغط على الزر أدناه:</p>
                                <div style="text-align: center;">
                                    <a href="$verificationLink" class="button">تفعيل الحساب</a>
                                </div>
                                <p>إذا لم يعمل الزر، يمكنك نسخ الرابط التالي ولصقه في المتصفح:</p>
                                <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 5px;">$verificationLink</p>
                                <p><strong>ملاحظة:</strong> هذا الرابط صالح لمدة 24 ساعة فقط.</p>
                                <p><strong>إذا لم تجد الإيميل:</strong> تحقق من مجلد الرسائل المزعجة (Spam)</p>
                            </div>
                            <div class="footer">
                                <p>تطبيق إرادة - حماية شاملة من المحتوى الضار</p>
                                <p>إذا لم تقم بالتسجيل في تطبيق إرادة، يرجى تجاهل هذا الإيميل.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                """.trimIndent()
                
                val jsonObject = JSONObject().apply {
                    put("service_id", emailjsServiceId)
                    put("template_id", emailjsTemplateId)
                    put("user_id", emailjsPublicKey)
                    put("template_params", JSONObject().apply {
                        put("to_email", userEmail)
                        put("from_email", "noreply@irada-app.com")
                        put("user_name", userName)
                        put("verification_link", verificationLink)
                        put("html_content", htmlContent)
                        put("subject", "تفعيل حسابك في تطبيق إرادة")
                    })
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonObject.toString().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .addHeader("Authorization", "Bearer $emailjsPrivateKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "IradaApp/1.0")
                    .post(body)
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                android.util.Log.e("IRADA_EMAILJS", "EmailJS Response Code: ${response.code}")
                android.util.Log.e("IRADA_EMAILJS", "EmailJS Response Body: $responseBody")
                
                response.isSuccessful
            }
        } catch (e: Exception) {
            android.util.Log.e("IRADA_EMAILJS", "❌ EmailJS Error: ${e.message}", e)
            false
        }
    }
    
    fun signInWithEmail(email: String, password: String, callback: (Boolean) -> Unit) {
        // التحقق من صحة الإيميل أولاً
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            android.util.Log.e("IRADA_AUTH", "❌ Invalid email format: $email")
            callback(false)
            return
        }
        
        android.util.Log.e("IRADA_AUTH", "📧 Signing in with email: $email")
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        android.util.Log.e("IRADA_AUTH", "✅ Sign in successful: $email")
                        
                        // التحقق من صحة الإيميل
                        if (user.isEmailVerified) {
                            android.util.Log.e("IRADA_AUTH", "✅ Email is verified")
                            saveUserToPreferences(user.uid, user.email ?: "", user.displayName ?: "")
                            callback(true)
                        } else {
                            android.util.Log.e("IRADA_AUTH", "❌ Email not verified - sending verification")
                            
                            // إرسال إيميل التحقق مرة أخرى
                            user.sendEmailVerification()
                                .addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        android.util.Log.e("IRADA_AUTH", "✅ Verification email sent again")
                                    }
                                }
                            
                            // تسجيل خروج المستخدم
                            auth.signOut()
                            android.util.Log.e("IRADA_AUTH", "🔒 User logged out - Email verification required")
                            callback(false) // فشل تسجيل الدخول - يحتاج تحقق
                        }
                    } else {
                        android.util.Log.e("IRADA_AUTH", "❌ User is null after sign in")
                        callback(false)
                    }
                } else {
                    android.util.Log.e("IRADA_AUTH", "❌ Sign in failed: ${task.exception?.message}")
                    callback(false)
                }
            }
    }
    
    fun registerWithEmail(name: String, email: String, password: String, callback: (Boolean) -> Unit) {
        // التحقق من صحة الإيميل أولاً
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            android.util.Log.e("IRADA_AUTH", "❌ Invalid email format: $email")
            callback(false)
            return
        }
        
        android.util.Log.e("IRADA_AUTH", "📧 Registering with email: $email")
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        android.util.Log.e("IRADA_AUTH", "✅ User created successfully: $email")
                        
                        // إرسال إيميل التحقق عبر Firebase Auth (الأفضل)
                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    android.util.Log.e("IRADA_AUTH", "✅ Firebase verification email sent to: $email")
                                    
                                    // Update display name
                                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                    
                                    user.updateProfile(profileUpdates)
                                        .addOnCompleteListener {
                                            // لا نحفظ المستخدم في Preferences حتى يتحقق من الإيميل
                                            saveUserToFirestore(user.uid, email, name)
                                            
                                            // تسجيل خروج المستخدم حتى يتحقق من الإيميل
                                            auth.signOut()
                                            
                                            android.util.Log.e("IRADA_AUTH", "🔒 User logged out - Email verification required")
                                            callback(true) // نجح التسجيل لكن يحتاج تحقق
                                        }
                                } else {
                                    android.util.Log.e("IRADA_AUTH", "❌ Failed to send Firebase verification email: ${verificationTask.exception?.message}")
                                    callback(false)
                                }
                            }
                    } else {
                        android.util.Log.e("IRADA_AUTH", "❌ User is null after creation")
                        callback(false)
                    }
                } else {
                    android.util.Log.e("IRADA_AUTH", "❌ Registration failed: ${task.exception?.message}")
                    callback(false)
                }
            }
    }
    
    fun signInWithGoogle(idToken: String, callback: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToPreferences(user.uid, user.email ?: "", user.displayName ?: "")
                        saveUserToFirestore(user.uid, user.email ?: "", user.displayName ?: "")
                        callback(true)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
    }
    
    // دالة لإرسال إيميل إعادة تعيين كلمة المرور
    fun sendPasswordResetEmail(email: String, callback: (Boolean) -> Unit) {
        // التحقق من صحة الإيميل أولاً
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            android.util.Log.e("IRADA_AUTH", "❌ Invalid email format: $email")
            callback(false)
            return
        }
        
        android.util.Log.e("IRADA_AUTH", "📧 Sending password reset email to: $email")
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.e("IRADA_AUTH", "✅ Password reset email sent successfully to: $email")
                    callback(true)
                } else {
                    android.util.Log.e("IRADA_AUTH", "❌ Failed to send password reset email: ${task.exception?.message}")
                    callback(false)
                }
            }
    }
    
    private fun saveUserToPreferences(uid: String, email: String, name: String) {
        // Set admin email first, then check if user is admin
        AdminManager.setAdminEmail(email)
        val isAdmin = AdminManager.isAdmin()
        
        val user = User(
            uid = uid,
            email = email,
            name = name,
            isPremium = isAdmin // Admin gets premium access
        )
        AppPreferences.saveUser(user)
    }
    
    private fun saveUserToFirestore(uid: String, email: String, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isAdmin = AdminManager.isAdmin()
            
            val user = User(
                uid = uid,
                email = email,
                name = name,
                isPremium = isAdmin // Admin gets premium access
            )
            
            firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnFailureListener { exception ->
                    // Handle error
                }
        }
    }
    
    fun signOut() {
        auth.signOut()
        AppPreferences.clearUser()
    }
}
