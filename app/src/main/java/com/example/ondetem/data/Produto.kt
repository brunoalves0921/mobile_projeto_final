package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Produto(
    @DocumentId val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    // MUDANÇA: O preço agora é um número que representa os centavos
    val precoEmCentavos: Long = 0,
    val lojaId: String = "",
    val lojaNome: String = "",
    val imagemUrl: String = "",
    val videoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val enderecoLoja: String = ""
) {
    @get:Exclude
    var distanciaEmMetros: Float? = null
}