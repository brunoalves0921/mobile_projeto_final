package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId

/**
 * Modelo de dados para Loja, ajustado para o Firestore.
 * @DocumentId faz com que o Firestore preencha este campo com o ID único do documento.
 */
data class Loja(
    @DocumentId val id: String = "", // ID único do documento no Firestore
    val nome: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val donoId: String = "" // ID do vendedor do Firebase Auth
)