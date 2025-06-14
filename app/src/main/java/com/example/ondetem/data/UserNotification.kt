package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val productId: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    // --- MUDANÇA AQUI ---
    // Adicionamos o campo para controlar se a notificação foi lida.
    val isRead: Boolean = false
)