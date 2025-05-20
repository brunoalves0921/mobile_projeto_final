package com.example.ondetem.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.ondetem.data.Produto
import com.example.ondetem.data.produtosMockados

class ProdutoViewModel : ViewModel() {

    var produtos by mutableStateOf(produtosMockados)
        private set

    var favoritos by mutableStateOf(listOf<Produto>())
        private set

    var busca by mutableStateOf("")
        private set

    fun buscar(texto: String) {
        busca = texto
        produtos = produtosMockados.filter {
            it.nome.contains(texto, ignoreCase = true)
        }
    }

    fun alternarFavorito(produto: Produto) {
        favoritos = if (favoritos.contains(produto)) {
            favoritos - produto
        } else {
            favoritos + produto
        }
    }

    fun limparFavoritos() {
        favoritos = listOf()
    }
}