// Nenhuma mudança necessária
// Caminho: app/src/main/java/com/example/ondetem/data/Loja.kt
package com.example.ondetem.data

import com.google.firebase.firestore.DocumentId

data class Loja(
    @DocumentId val id: String = "",
    val nome: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val donoId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)