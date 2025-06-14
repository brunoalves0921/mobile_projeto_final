package com.example.ondetem.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ondetem.data.PriceAlertRepository
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.VideoPlayer
import com.example.ondetem.ui.utils.formatPrice
import com.example.ondetem.viewmodel.ProdutoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetalhesScreen(
    produtoId: String,
    viewModel: ProdutoViewModel,
    favoriteProducts: List<Produto>,
    onToggleFavorite: (Produto) -> Unit,
    areNotificationsEnabled: Boolean
) {
    val context = LocalContext.current
    var produto by remember { mutableStateOf<Produto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // --- ESTADOS PARA O NOVO DIÁLOGO DE ALERTA ---
    var showPriceAlertDialog by remember { mutableStateOf(false) }
    var desiredPrice by remember { mutableStateOf("") }

    LaunchedEffect(produtoId) {
        isLoading = true
        produto = viewModel.todosOsProdutos.value.firstOrNull { it.id == produtoId }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (produto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Produto não encontrado.") }
    } else {
        // --- DIÁLOGO PARA CRIAR ALERTA DE PREÇO ---
        if (showPriceAlertDialog) {
            AlertDialog(
                onDismissRequest = { showPriceAlertDialog = false },
                title = { Text("Criar Alerta de Preço") },
                text = {
                    Column {
                        Text("Avise-me quando o preço de '${produto!!.nome}' for menor ou igual a:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = desiredPrice,
                            onValueChange = { desiredPrice = it.filter { char -> char.isDigit() } },
                            label = { Text("Preço desejado (Ex: 1990)") },
                            prefix = { Text("R$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val priceInCents = desiredPrice.toLongOrNull()
                            if (priceInCents == null || priceInCents <= 0) {
                                Toast.makeText(context, "Por favor, insira um preço válido.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                PriceAlertRepository.salvarAlerta(produto!!, priceInCents)
                                Toast.makeText(context, "Alerta de preço criado com sucesso!", Toast.LENGTH_LONG).show()
                                desiredPrice = ""
                                showPriceAlertDialog = false
                            }
                        }
                    ) { Text("Salvar Alerta") }
                },
                dismissButton = {
                    TextButton(onClick = { showPriceAlertDialog = false }) { Text("Cancelar") }
                }
            )
        }


        // Layout principal da tela
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(produto!!.nome, style = MaterialTheme.typography.headlineMedium)
            Text(produto!!.descricao, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Preço: ${formatPrice(produto!!.precoEmCentavos)}")
            Text("Vendido por: ${produto!!.lojaNome}", style = MaterialTheme.typography.titleSmall)

            if (produto!!.enderecoLoja.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth().clickable {
                        val gmmIntentUri = Uri.parse("geo:${produto!!.latitude},${produto!!.longitude}?q=${Uri.encode(produto!!.enderecoLoja)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.LocationOn, "Endereço", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = produto!!.enderecoLoja, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (produto!!.imagemUrl.isNotBlank()) {
                AsyncImage(model = produto!!.imagemUrl, "Imagem", Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (produto!!.videoUrl.isNotBlank()) {
                VideoPlayer(videoUrl = produto!!.videoUrl, modifier = Modifier.fillMaxWidth().height(220.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            val isFavorito = favoriteProducts.any { it.id == produto!!.id }
            val escalaIcone by animateFloatAsState(targetValue = if (isFavorito) 1.2f else 1.0f, label = "")

            Button(onClick = { onToggleFavorite(produto!!) }) {
                Icon(if (isFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favoritar", tint = if (isFavorito) MaterialTheme.colorScheme.error else LocalContentColor.current, modifier = Modifier.scale(escalaIcone))
                Spacer(Modifier.width(8.dp))
                Text(if (isFavorito) "Remover dos Favoritos" else "Adicionar aos Favoritos")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // --- BOTÃO ATUALIZADO PARA O NOVO SISTEMA ---
            OutlinedButton(onClick = {
                if (!areNotificationsEnabled) {
                    Toast.makeText(context, "As notificações estão desativadas nas configurações do app.", Toast.LENGTH_SHORT).show()
                    return@OutlinedButton
                }
                // Abre o diálogo para criar o alerta
                showPriceAlertDialog = true
            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Criar Alerta de Preço")
                Spacer(Modifier.width(8.dp))
                Text("Notificar Queda de Preço")
            }
        }
    }
}