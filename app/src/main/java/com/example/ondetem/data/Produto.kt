package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId

data class Produto(
    @DocumentId val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val preco: String = "",
    val lojaId: String = "",
    val lojaNome: String = "",
    val imagemUrl: String = "",
    val videoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)