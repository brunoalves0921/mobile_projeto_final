package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId

/**
 * Modelo de dados para Produto, ajustado para o Firestore.
 */
data class Produto(
    @DocumentId val id: String = "", // ID único do documento no Firestore
    val nome: String = "",
    val descricao: String = "",
    val preco: String = "",
    val lojaId: String = "",    // ID do documento da loja a que pertence
    val lojaNome: String = "",  // Mantemos o nome da loja para exibição fácil
    val imagemUrl: String = "", // URL da imagem no Cloud Storage
    val videoUrl: String = ""   // URL do vídeo no Cloud Storage
)