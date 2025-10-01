package com.irada.blockerheroar.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.irada.blockerheroar.data.BlockedContent
import com.irada.blockerheroar.data.ContentType
import kotlinx.coroutines.tasks.await

object BlockedContentManager {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getUserId(): String? = auth.currentUser?.uid
    
    private fun getBlockedContentCollection() = 
        getUserId()?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .collection("blocked_content")
        }
    
    // حفظ محتوى محظور جديد
    suspend fun addBlockedContent(content: BlockedContent): Boolean {
        return try {
            getBlockedContentCollection()
                ?.document(content.id)
                ?.set(content)
                ?.await()
            true
        } catch (e: Exception) {
            android.util.Log.e("BlockedContentManager", "Error adding content: ${e.message}")
            false
        }
    }
    
    // حذف محتوى محظور
    suspend fun removeBlockedContent(contentId: String): Boolean {
        return try {
            getBlockedContentCollection()
                ?.document(contentId)
                ?.delete()
                ?.await()
            true
        } catch (e: Exception) {
            android.util.Log.e("BlockedContentManager", "Error removing content: ${e.message}")
            false
        }
    }
    
    // جلب جميع المحتويات المحظورة
    suspend fun getAllBlockedContent(): List<BlockedContent> {
        return try {
            val snapshot = getBlockedContentCollection()
                ?.get()
                ?.await()
            
            snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(BlockedContent::class.java)
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("BlockedContentManager", "Error fetching content: ${e.message}")
            emptyList()
        }
    }
    
    // جلب محتويات محظورة حسب النوع
    suspend fun getBlockedContentByType(type: ContentType): List<BlockedContent> {
        return try {
            val snapshot = getBlockedContentCollection()
                ?.whereEqualTo("type", type.name)
                ?.get()
                ?.await()
            
            snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(BlockedContent::class.java)
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("BlockedContentManager", "Error fetching by type: ${e.message}")
            emptyList()
        }
    }
    
    // تحديث حالة محتوى محظور
    suspend fun updateContentStatus(contentId: String, isActive: Boolean): Boolean {
        return try {
            getBlockedContentCollection()
                ?.document(contentId)
                ?.update("isActive", isActive)
                ?.await()
            true
        } catch (e: Exception) {
            android.util.Log.e("BlockedContentManager", "Error updating status: ${e.message}")
            false
        }
    }
}