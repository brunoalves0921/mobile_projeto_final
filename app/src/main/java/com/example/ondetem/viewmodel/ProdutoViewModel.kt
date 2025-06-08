package com.example.ondetem.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondetem.data.produtosMockados
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProdutoViewModel : ViewModel() {

    var produtos by mutableStateOf(produtosMockados)
        private set

    var busca by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun buscar(texto: String) {
        busca = texto
        viewModelScope.launch {
            isLoading = true
            delay(1000)
            produtos = if (texto.isNotBlank()) {
                produtosMockados.filter {
                    it.nome.contains(texto, ignoreCase = true)
                }
            } else {
                produtosMockados
            }
            isLoading = false
        }
    }
}