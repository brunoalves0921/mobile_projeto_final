package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserNotification(
    // --- MUDANÇA AQUI ---
    // Adicionamos o ID para podermos deletar a notificação específica.
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val productId: String = "",
    @ServerTimestamp val timestamp: Date? = null
)