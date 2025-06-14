package com.example.ondetem.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserNotification(
    val title: String = "",
    val body: String = "",
    val productId: String = "",
    @ServerTimestamp val timestamp: Date? = null
)