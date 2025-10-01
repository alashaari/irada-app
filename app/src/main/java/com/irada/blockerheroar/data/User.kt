package com.irada.blockerheroar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val isPremium: Boolean = false,
    val subscriptionEndDate: Long = 0L,
    val partnerEmail: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
