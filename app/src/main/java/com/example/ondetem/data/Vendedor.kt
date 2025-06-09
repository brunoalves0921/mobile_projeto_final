package com.example.ondetem.data

/**
 * Modelo de dados para o Vendedor.
 * O 'uid' é o ID único do Firebase Authentication.
 * Os outros campos são informações adicionais que salvaremos no Firestore.
 * O construtor vazio é necessário para o Firestore converter os dados de volta para um objeto.
 */
data class Vendedor(
    val uid: String = "",
    val nome: String = "",
    val email: String = ""
)