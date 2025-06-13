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
// Importações necessárias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProdutoViewModel(application: Application) : AndroidViewModel(application) {

    val produtos = mutableStateListOf<Produto>()

    // ALTERAÇÃO 1: Transformar a lista mestra em um StateFlow.
    // O _todosOsProdutos é privado e mutável.
    private val _todosOsProdutos = MutableStateFlow<List<Produto>>(emptyList())
    // O todosOsProdutos é público e imutável (só leitura), para ser observado pela UI.
    val todosOsProdutos = _todosOsProdutos.asStateFlow()

    var busca by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("Carregando produtos...")
        private set

    private var ultimaLocalizacaoUsuario: Location? = null
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        carregarTodosOsProdutos()
    }

    fun carregarTodosOsProdutos() {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Buscando produtos..."
            // ALTERAÇÃO 2: O resultado do repositório agora é emitido para o StateFlow.
            _todosOsProdutos.value = ProdutoRepository.listarTodos()
            busca = ""
            produtos.clear()
            isLoading = false
        }
    }

    @SuppressLint("MissingPermission")
    fun ordenarPorProximidade() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userLocation = ultimaLocalizacaoUsuario ?: run {
                    statusMessage = "Obtendo sua localização..."
                    val locationResult = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()

                    if (locationResult != null) {
                        ultimaLocalizacaoUsuario = locationResult
                        locationResult
                    } else {
                        Toast.makeText(getApplication(), "Não foi possível obter a localização.", Toast.LENGTH_LONG).show()
                        null
                    }
                }

                if (userLocation != null) {
                    statusMessage = "Calculando distâncias..."
                    // ALTERAÇÃO 3: Acessamos e atualizamos a lista através do .value do StateFlow.
                    _todosOsProdutos.value = _todosOsProdutos.value.map { produto ->
                        val lojaLocation = Location("Loja").apply {
                            latitude = produto.latitude
                            longitude = produto.longitude
                        }
                        produto.distanciaEmMetros = userLocation.distanceTo(lojaLocation)
                        produto
                    }.sortedBy { it.distanciaEmMetros }

                    buscar(busca)
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
            // ALTERAÇÃO 4: Filtramos a lista a partir do .value do StateFlow.
            _todosOsProdutos.value.filter {
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