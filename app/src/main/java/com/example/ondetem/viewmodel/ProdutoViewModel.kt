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

    // Lista de produtos para a UI. É um mutableStateListOf para garantir que a UI
    // reaja a mudanças (adição/remoção de itens).
    val produtos = mutableStateListOf<Produto>()

    // Lista mestra interna com todos os produtos.
    var todosOsProdutos = listOf<Produto>()

    // O texto da busca.
    var busca by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf("Carregando produtos...")
        private set

    // Guarda a última localização obtida para não buscar novamente.
    private var ultimaLocalizacaoUsuario: Location? = null
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        carregarTodosOsProdutos()
    }

    fun carregarTodosOsProdutos() {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Buscando produtos..."
            todosOsProdutos = ProdutoRepository.listarTodos()
            // Garante que, ao carregar, a busca e a lista de exibição estejam limpas.
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
                // Se já temos a localização, usamos ela. Se não, buscamos.
                val userLocation = ultimaLocalizacaoUsuario ?: run {
                    statusMessage = "Obtendo sua localização..."
                    val locationResult = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()

                    if (locationResult != null) {
                        ultimaLocalizacaoUsuario = locationResult // Armazena para uso futuro
                        locationResult
                    } else {
                        Toast.makeText(getApplication(), "Não foi possível obter a localização.", Toast.LENGTH_LONG).show()
                        null
                    }
                }

                if (userLocation != null) {
                    statusMessage = "Calculando distâncias..."
                    // Apenas atualiza a lista mestra com as distâncias e a nova ordem.
                    todosOsProdutos = todosOsProdutos.map { produto ->
                        val lojaLocation = Location("Loja").apply {
                            latitude = produto.latitude
                            longitude = produto.longitude
                        }
                        produto.distanciaEmMetros = userLocation.distanceTo(lojaLocation)
                        produto
                    }.sortedBy { it.distanciaEmMetros }

                    // APLICA a busca atual à nova lista ordenada.
                    // Se a busca estiver vazia, a lista de exibição continuará vazia.
                    buscar(busca)

                    //Toast.makeText(getApplication(), "Localização obtida! Agora pesquise para ver os produtos mais próximos.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Erro ao obter localização: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Função de busca ATUALIZADA.
     * Agora ela é a única responsável por popular a lista 'produtos'.
     */
    fun buscar(texto: String) {
        busca = texto

        // Se o texto da busca não estiver vazio, filtra a lista mestra.
        // Se estiver vazio, o resultado é uma lista vazia.
        val resultados = if (texto.isNotBlank()) {
            todosOsProdutos.filter {
                it.nome.contains(texto, ignoreCase = true) ||
                        it.descricao.contains(texto, ignoreCase = true)
            }
        } else {
            emptyList()
        }

        // Atualiza a lista da UI de forma explícita.
        produtos.clear()
        produtos.addAll(resultados)
    }
}