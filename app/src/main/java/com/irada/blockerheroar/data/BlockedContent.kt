package com.irada.blockerheroar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlockedContent(
    val id: String = "",
    val type: ContentType = ContentType.KEYWORD,
    val content: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class ContentType {
    KEYWORD,
    WEBSITE,
    APP
}
