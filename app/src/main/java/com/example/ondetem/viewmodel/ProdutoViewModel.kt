package com.example.ondetem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProdutoViewModel(application: Application) : AndroidViewModel(application) {

    // A lista de produtos exibida na UI. Começa vazia.
    var produtos by mutableStateOf<List<Produto>>(emptyList())
        private set

    // A lista mestra com todos os produtos.
    var todosOsProdutos by mutableStateOf<List<Produto>>(emptyList())
        private set

    var busca by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("Carregando produtos...")
        private set

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        carregarTodosOsProdutos()
    }

    fun carregarTodosOsProdutos() {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Buscando produtos..."
            todosOsProdutos = ProdutoRepository.listarTodos()
            isLoading = false
            // Garante que a lista de exibição comece vazia
            buscar(busca)
        }
    }

    @SuppressLint("MissingPermission")
    fun ordenarEExibirPorProximidade() {
        viewModelScope.launch {
            try {
                isLoading = true
                statusMessage = "Obtendo sua localização..."
                val locationResult = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()

                if (locationResult != null) {
                    statusMessage = "Calculando distâncias..."
                    val userLocation = Location("User").apply {
                        latitude = locationResult.latitude
                        longitude = locationResult.longitude
                    }

                    val listaOrdenada = todosOsProdutos.sortedBy { produto ->
                        val lojaLocation = Location("Loja").apply {
                            latitude = produto.latitude
                            longitude = produto.longitude
                        }
                        userLocation.distanceTo(lojaLocation)
                    }
                    // ATUALIZAÇÃO: Define a lista de exibição com os produtos ordenados
                    produtos = listaOrdenada

                } else {
                    Toast.makeText(getApplication(), "Não foi possível obter a localização.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Erro ao obter localização: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun buscar(texto: String) {
        busca = texto
        // AQUI ESTÁ A CORREÇÃO:
        // A lista de `produtos` só é preenchida se o usuário digitou algo.
        produtos = if (texto.isNotBlank()) {
            todosOsProdutos.filter {
                it.nome.contains(texto, ignoreCase = true) ||
                        it.descricao.contains(texto, ignoreCase = true)
            }
        } else {
            // Se a busca estiver vazia, a lista de exibição também fica vazia.
            emptyList()
        }
    }
}