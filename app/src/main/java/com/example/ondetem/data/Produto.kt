package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId

data class Produto(
    @DocumentId
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val precoEmCentavos: Long = 0,
    val lojaId: String = "",
    val lojaNome: String = "",
    val enderecoLoja: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val videoUrl: String = "",

    val primaryImageUrl: String = "",
    val imageUrls: List<String> = listOf(),

    // Campo para busca em minúsculas
    val nomeMinusculo: String = nome.lowercase(),

    // Este campo não é salvo no DB, apenas usado localmente para ordenação.
    @Transient
    var distanciaEmMetros: Float? = null
) {
    // Construtor vazio necessário para o Firestore
    constructor() : this(
        id = "",
        nome = "",
        descricao = "",
        precoEmCentavos = 0,
        lojaId = "",
        lojaNome = "",
        enderecoLoja = "",
        latitude = 0.0,
        longitude = 0.0,
        videoUrl = "",
        primaryImageUrl = "",
        imageUrls = listOf()
    )
}