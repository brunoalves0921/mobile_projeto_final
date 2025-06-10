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

    val produtos = mutableStateListOf<Produto>()

    // AQUI ESTÁ A CORREÇÃO:
    // Tornamos a lista pública para leitura, mas apenas o ViewModel pode alterá-la.
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
            produtos.clear()
            isLoading = false
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

                    val listaOrdenada = todosOsProdutos.map { produto ->
                        val lojaLocation = Location("Loja").apply {
                            latitude = produto.latitude
                            longitude = produto.longitude
                        }
                        produto.distanciaEmMetros = userLocation.distanceTo(lojaLocation)
                        produto
                    }.sortedBy { it.distanciaEmMetros }

                    todosOsProdutos = listaOrdenada.toList()

                    buscar(busca)

                    Toast.makeText(getApplication(), "Pronto! Agora pesquise para ver os produtos mais próximos.", Toast.LENGTH_LONG).show()
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
        val resultados = if (texto.isNotBlank()) {
            todosOsProdutos.filter {
                it.nome.contains(texto, ignoreCase = true) ||
                        it.descricao.contains(texto, ignoreCase = true)
            }
        } else {
            emptyList()
        }
        produtos.clear()
        produtos.addAll(resultados)
    }
}