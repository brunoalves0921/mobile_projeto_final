package com.example.ondetem.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ondetem.data.LojaRepository
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

    private val repository = ProdutoRepository

    val produtos = mutableStateListOf<Produto>()

    private val _todosOsProdutos = MutableStateFlow<List<Produto>>(emptyList())
    val todosOsProdutos = _todosOsProdutos.asStateFlow()

    var busca by mutableStateOf("")
        private set

    private val _isListLoading = MutableStateFlow(false)
    val isListLoading = _isListLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    var statusMessage by mutableStateOf("Carregando produtos...")
        private set

    private var ultimaLocalizacaoUsuario: Location? = null
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        viewModelScope.launch {
            _isListLoading.value = true
            statusMessage = "Carregando produtos..."
            repository.listarTodosFlow().collect { produtosAtualizados ->
                _todosOsProdutos.value = produtosAtualizados
                if (busca.isNotBlank()) {
                    buscar(busca)
                }
                _isListLoading.value = false
            }
        }
    }

    fun getProdutoById(produtoId: String, onResult: (Produto?) -> Unit) {
        viewModelScope.launch {
            val produto = repository.getProdutoById(produtoId)
            onResult(produto)
        }
    }

    fun salvarProdutoCompleto(
        produtoId: String?,
        nome: String,
        descricao: String,
        preco: String,
        lojaId: String,
        existingImageUrls: List<String>,
        newImageUris: List<Uri>,
        primaryImage: Any?,
        localVideoUri: Uri?,
        remoteVideoUrl: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true

            val newlyUploadedUrls = if (newImageUris.isNotEmpty()) {
                repository.uploadImages(newImageUris)
            } else { emptyList() }
            val finalImageUrls = existingImageUrls + newlyUploadedUrls

            var finalPrimaryImageUrl = ""
            when (primaryImage) {
                is String -> finalPrimaryImageUrl = primaryImage
                is Uri -> {
                    val primaryIndex = newImageUris.indexOf(primaryImage)
                    if (primaryIndex != -1 && primaryIndex < newlyUploadedUrls.size) {
                        finalPrimaryImageUrl = newlyUploadedUrls[primaryIndex]
                    }
                }
            }

            if (finalPrimaryImageUrl.isBlank() && finalImageUrls.isNotEmpty()) {
                finalPrimaryImageUrl = finalImageUrls.first()
            }

            var finalVideoUrl = ""
            if (localVideoUri != null) {
                val uploadedVideoUrl = repository.uploadImages(listOf(localVideoUri))
                if (uploadedVideoUrl.isNotEmpty()) {
                    finalVideoUrl = uploadedVideoUrl.first()
                }
            } else {
                finalVideoUrl = remoteVideoUrl
            }

            val loja = LojaRepository.getLojaPorId(lojaId)
            if (loja == null) {
                Toast.makeText(getApplication(), "Erro: Loja não encontrada.", Toast.LENGTH_SHORT).show()
                _isSaving.value = false
                return@launch
            }

            // ================================================================
            // ===== CORREÇÃO NA LÓGICA DO PREÇO ==============================
            // ================================================================
            // A variável 'preco' já contém o valor em centavos.
            // Apenas convertemos para Long.
            val precoFinalEmCentavos = preco.toLongOrNull() ?: 0L

            val produto = Produto(
                id = produtoId ?: "",
                nome = nome,
                descricao = descricao,
                precoEmCentavos = precoFinalEmCentavos, // <-- CORREÇÃO AQUI
                lojaId = loja.id,
                lojaNome = loja.nome,
                enderecoLoja = loja.endereco,
                latitude = loja.latitude,
                longitude = loja.longitude,
                imageUrls = finalImageUrls,
                primaryImageUrl = finalPrimaryImageUrl,
                videoUrl = finalVideoUrl
            )

            repository.salvarProduto(produto)

            _isSaving.value = false
            Toast.makeText(getApplication(), "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    @SuppressLint("MissingPermission")
    fun ordenarPorProximidade() {
        viewModelScope.launch {
            _isListLoading.value = true
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
                _isListLoading.value = false
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