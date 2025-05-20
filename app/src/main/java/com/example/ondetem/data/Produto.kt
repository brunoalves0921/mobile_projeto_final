package com.example.ondetem.data


data class Produto(
    val id: Int,
    val nome: String,
    val descricao: String,
    val preco: String,
    val imagemUrl: String,
    val loja: String,
    val endereco: String,
    val telefone: String,
    val videoUrl: String = ""
)

