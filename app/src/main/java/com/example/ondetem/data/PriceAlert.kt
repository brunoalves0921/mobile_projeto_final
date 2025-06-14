package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa um alerta de preço criado por um usuário para um produto específico.
 *
 * @param id ID automático do documento no Firestore.
 * @param userId ID do usuário que criou o alerta (do Firebase Auth).
 * @param productId ID do produto que está sendo monitorado.
 * @param produtoNome Nome do produto (salvo para fácil exibição nas notificações).
 * @param desiredPriceInCents O preço (em centavos) que o usuário deseja ser notificado.
 * @param createdAt Data em que o alerta foi criado.
 */
data class PriceAlert(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val produtoNome: String = "",
    val desiredPriceInCents: Long = 0,
    @ServerTimestamp val createdAt: Date? = null
)