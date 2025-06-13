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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProdutoViewModel(application: Application) : AndroidViewModel(application) {

    val produtos = mutableStateListOf<Produto>()

    private val _todosOsProdutos = MutableStateFlow<List<Produto>>(emptyList())
    val todosOsProdutos = _todosOsProdutos.asStateFlow()

    var busca by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("Carregando produtos...")
        private set

    private var ultimaLocalizacaoUsuario: Location? = null
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // --- MUDANÇA PRINCIPAL ---
    init {
        // Agora o ViewModel se "inscreve" no fluxo de dados em tempo real.
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Carregando produtos..."
            // Coleta o fluxo do repositório. Sempre que a lista no Firestore mudar,
            // o código dentro do `collect` será executado com a nova lista.
            ProdutoRepository.listarTodosFlow().collect { produtosAtualizados ->
                _todosOsProdutos.value = produtosAtualizados

                // Se o usuário estiver com uma busca ativa, re-aplicamos o filtro
                // para que a lista de resultados também seja atualizada.
                if (busca.isNotBlank()) {
                    buscar(busca)
                }
                isLoading = false // Desliga o loading após a primeira carga de dados
            }
        }
    }

    // A função carregarTodosOsProdutos() não é mais necessária, pois o init já é reativo.
    // fun carregarTodosOsProdutos() { ... }

    // NENHUMA MUDANÇA NECESSÁRIA AQUI.
    // As funções abaixo já usam `_todosOsProdutos.value`, que agora está sempre atualizada.
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