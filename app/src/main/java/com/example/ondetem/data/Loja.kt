package com.example.ondetem.data

data class Loja(
    val nome: String,
    val endereco: String,
    val telefone: String,
    val donoEmail: String // Para vincular a loja ao vendedor
)