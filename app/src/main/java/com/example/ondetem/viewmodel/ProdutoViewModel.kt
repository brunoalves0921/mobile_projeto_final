package com.example.ondetem.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import kotlinx.coroutines.launch

class ProdutoViewModel(application: Application) : AndroidViewModel(application) {

    var produtos by mutableStateOf<List<Produto>>(emptyList())
        private set

    var todosOsProdutos by mutableStateOf<List<Produto>>(emptyList())
        private set

    var busca by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        carregarTodosOsProdutos()
    }

    fun carregarTodosOsProdutos() {
        viewModelScope.launch {
            isLoading = true
            todosOsProdutos = ProdutoRepository.listarTodos()
            // Re-aplica a busca se já houver uma
            buscar(busca)
            isLoading = false
        }
    }

    fun buscar(texto: String) {
        busca = texto
        // Não precisa de coroutine aqui, a filtragem é síncrona
        produtos = if (texto.isNotBlank()) {
            todosOsProdutos.filter {
                it.nome.contains(texto, ignoreCase = true) ||
                        it.descricao.contains(texto, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }
}