package com.irada.blockerheroar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PartnerRequest(
    val id: String = "",
    val requesterEmail: String = "",
    val partnerEmail: String = "",
    val requestType: RequestType = RequestType.DISABLE_FEATURE,
    val featureName: String = "",
    val message: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long = 0L
) : Parcelable

enum class RequestType {
    DISABLE_FEATURE,
    REMOVE_PROTECTION,
    UNBLOCK_CONTENT
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
