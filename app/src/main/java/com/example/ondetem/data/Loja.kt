package com.example.ondetem.data

/**
 * Modelo de dados para Loja, ajustado para o Firestore.
 * 'donoId' agora guarda o ID único do vendedor do Firebase Authentication.
 * O construtor vazio é necessário para o Firestore.
 */
data class Loja(
    val nome: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val donoId: String = "" // Campo renomeado de donoEmail para donoId
)