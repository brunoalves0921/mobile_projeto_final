package com.example.ondetem.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProdutoViewModel(application: Application) : AndroidViewModel(application) {

    var produtos by mutableStateOf<List<Produto>>(emptyList())
        private set

    // MUDANÇA: Tornando a lista mestra acessível publicamente (apenas para leitura)
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
            val context = getApplication<Application>().applicationContext
            // Agora atualiza a nova variável de estado pública
            todosOsProdutos = ProdutoRepository.listarTodos(context)
        }
    }

    fun buscar(texto: String) {
        busca = texto
        viewModelScope.launch {
            isLoading = true
            delay(500)
            produtos = if (texto.isNotBlank()) {
                todosOsProdutos.filter {
                    it.nome.contains(texto, ignoreCase = true) ||
                            it.descricao.contains(texto, ignoreCase = true)
                }
            } else {
                emptyList()
            }
            isLoading = false
        }
    }
}