package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Loja
import com.example.ondetem.data.LojaRepository
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CadastroLojaScreen(
    vendedorId: String?,
    lojaId: String?,
    onLojaSalva: () -> Unit
) {
    val isEditMode = lojaId != null
    var nomeLoja by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(isEditMode) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val placesClient = remember { Places.createClient(context) }
    val token = remember { AutocompleteSessionToken.newInstance() }

    var enderecoQuery by remember { mutableStateOf("") }
    var sugestoes by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(true) }

    var latitudeSelecionada by remember { mutableStateOf<Double?>(null) }
    var longitudeSelecionada by remember { mutableStateOf<Double?>(null) }

    var lojaCarregada by remember { mutableStateOf<Loja?>(null) }

    // Carrega os dados da loja se estiver em modo de edição
    LaunchedEffect(lojaId) {
        if (isEditMode && lojaId != null) {
            val loja = LojaRepository.getLojaPorId(lojaId)
            if (loja != null) {
                lojaCarregada = loja
                nomeLoja = loja.nome
                telefone = loja.telefone
                enderecoQuery = loja.endereco
                latitudeSelecionada = loja.latitude
                longitudeSelecionada = loja.longitude
                showSuggestions = false
            }
            isLoading = false
        }
    }

    // Busca sugestões quando o usuário digita
    LaunchedEffect(enderecoQuery) {
        if (enderecoQuery.length > 2 && showSuggestions) {
            try {
                val predictions = LojaRepository.buscarSugestoesDeEndereco(placesClient, enderecoQuery, token).await()
                sugestoes = predictions
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Erro de API: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            sugestoes = emptyList()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (o resto da UI que já está correta, incluindo o botão de salvar)
        Text(if (isEditMode) "Editar Loja" else "Cadastrar Nova Loja", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = nomeLoja, onValueChange = { nomeLoja = it }, label = { Text("Nome da Loja") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = enderecoQuery,
            onValueChange = {
                enderecoQuery = it
                latitudeSelecionada = null
                showSuggestions = true
            },
            label = { Text("Digite o endereço da loja") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (sugestoes.isNotEmpty()) {
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(sugestoes) { sugestao ->
                    Text(
                        text = sugestao.getFullText(null).toString(),
                        modifier = Modifier.fillMaxWidth().clickable {
                            scope.launch {
                                try {
                                    val place = LojaRepository.buscarDetalhesDoLocal(placesClient, sugestao.placeId).await()
                                    showSuggestions = false
                                    enderecoQuery = place.address ?: ""
                                    latitudeSelecionada = place.latLng?.latitude
                                    longitudeSelecionada = place.latLng?.longitude
                                    sugestoes = emptyList()
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }.padding(16.dp)
                    )
                    Divider()
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = telefone, onValueChange = { telefone = it }, label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (nomeLoja.isNotBlank() && enderecoQuery.isNotBlank() && telefone.isNotBlank() && latitudeSelecionada != null) {
                    isLoading = true
                    scope.launch {
                        try {
                            if (isEditMode) {
                                val lojaAtualizada = lojaCarregada!!.copy(
                                    nome = nomeLoja,
                                    endereco = enderecoQuery,
                                    telefone = telefone,
                                    latitude = latitudeSelecionada!!,
                                    longitude = longitudeSelecionada!!
                                )
                                LojaRepository.atualizar(lojaAtualizada)
                                Toast.makeText(context, "Loja atualizada!", Toast.LENGTH_SHORT).show()
                            } else {
                                val novaLoja = Loja(nome = nomeLoja, endereco = enderecoQuery, telefone = telefone, donoId = vendedorId!!, latitude = latitudeSelecionada!!, longitude = longitudeSelecionada!!)
                                LojaRepository.salvar(novaLoja)
                                Toast.makeText(context, "Loja cadastrada!", Toast.LENGTH_SHORT).show()
                            }
                            onLojaSalva()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
            else { Text(if (isEditMode) "Salvar Alterações" else "Salvar Loja") }
        }
    }
}