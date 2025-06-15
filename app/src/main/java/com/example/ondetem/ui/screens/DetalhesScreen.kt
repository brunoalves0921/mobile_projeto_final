package com.example.ondetem.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ondetem.data.PriceAlertRepository
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.VideoPlayer
import com.example.ondetem.ui.utils.CurrencyVisualTransformation
// ================================================================
// ===== IMPORT NECESSÁRIO PARA A NOVA FUNÇÃO =====================
// ================================================================
import com.example.ondetem.ui.utils.getThumbnailUrl
import com.example.ondetem.viewmodel.ProdutoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
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

    var showPriceAlertDialog by remember { mutableStateOf(false) }
    var desiredPrice by remember { mutableStateOf("") }

    LaunchedEffect(produtoId) {
        isLoading = true
        viewModel.getProdutoById(produtoId) { p ->
            produto = p
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (produto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Produto não encontrado.") }
    } else {
        val produtoNaoNulo = produto!!

        if (showPriceAlertDialog) {
            AlertDialog(
                onDismissRequest = { showPriceAlertDialog = false },
                title = { Text("Criar Alerta de Preço") },
                text = {
                    Column {
                        Text("Avise-me quando o preço de '${produtoNaoNulo.nome}' for menor ou igual a:")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = desiredPrice,
                            onValueChange = {
                                val newText = it.filter { char -> char.isDigit() }
                                if (newText.length <= 10) { desiredPrice = newText }
                            },
                            label = { Text("Preço desejado") },
                            visualTransformation = CurrencyVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val priceInCents = desiredPrice.toLongOrNull()
                        if (priceInCents == null || priceInCents <= 0) {
                            Toast.makeText(context, "Por favor, insira um preço válido.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            PriceAlertRepository.salvarAlerta(produtoNaoNulo, priceInCents)
                            Toast.makeText(context, "Alerta de preço criado com sucesso!", Toast.LENGTH_LONG).show()
                            desiredPrice = ""
                            showPriceAlertDialog = false
                        }
                    }) { Text("Salvar Alerta") }
                },
                dismissButton = {
                    TextButton(onClick = { showPriceAlertDialog = false }) { Text("Cancelar") }
                }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                val imageUrls = remember(produtoNaoNulo) {
                    produtoNaoNulo.imageUrls
                }
                val videoUrl = remember(produtoNaoNulo) {
                    produtoNaoNulo.videoUrl.takeIf { it.isNotBlank() }
                }

                val tabItems = remember(imageUrls, videoUrl) {
                    buildList {
                        if (imageUrls.isNotEmpty()) add(if (imageUrls.size > 1) "Imagens" else "Imagem")
                        if (videoUrl != null) add("Vídeo")
                    }
                }

                if (tabItems.isNotEmpty()) {
                    var selectedTabIndex by remember { mutableStateOf(0) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (tabItems.size > 1) {
                            TabRow(selectedTabIndex = selectedTabIndex) {
                                tabItems.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { selectedTabIndex = index },
                                        text = { Text(title) }
                                    )
                                }
                            }
                        }

                        AnimatedContent(
                            targetState = selectedTabIndex,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(Color.Black),
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            }, label = "mediaAnimation"
                        ) { tabIndex ->
                            when (tabItems[tabIndex]) {
                                "Imagem", "Imagens" -> {
                                    if (imageUrls.size > 1) {
                                        val pagerState = rememberPagerState { imageUrls.size }
                                        Box(contentAlignment = Alignment.BottomCenter) {
                                            HorizontalPager(
                                                state = pagerState,
                                                modifier = Modifier.fillMaxSize()
                                            ) { page ->
                                                AsyncImage(
                                                    // Carrega a imagem grande para a galeria
                                                    model = getThumbnailUrl(imageUrls[page], "1080x1080"),
                                                    contentDescription = "Imagem do produto ${page + 1}",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Row(
                                                Modifier
                                                    .height(50.dp)
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                repeat(imageUrls.size) { iteration ->
                                                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(4.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .size(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        AsyncImage(
                                            // Carrega a imagem grande para a visualização única
                                            model = getThumbnailUrl(imageUrls.firstOrNull() ?: "", "1080x1080"),
                                            contentDescription = "Imagem do produto",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                "Vídeo" -> {
                                    if (videoUrl != null) {
                                        VideoPlayer(videoUrl = videoUrl, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ImageNotSupported, "Sem imagem", modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = produtoNaoNulo.nome,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        val isFavorito = favoriteProducts.any { it.id == produtoNaoNulo.id }
                        Icon(
                            imageVector = if (isFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (isFavorito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onToggleFavorite(produtoNaoNulo) }
                                )
                                .padding(start = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "R$ ${"%.2f".format(produtoNaoNulo.precoEmCentavos / 100.0)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val postNotificationPermission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                    Button(
                        onClick = {
                            if (!areNotificationsEnabled) {
                                Toast.makeText(context, "As notificações estão desativadas nas configurações do app.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postNotificationPermission.status.isGranted) {
                                postNotificationPermission.launchPermissionRequest()
                            } else {
                                showPriceAlertDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Criar Alerta")
                    }
                    if (produtoNaoNulo.enderecoLoja.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("geo:${produtoNaoNulo.latitude},${produtoNaoNulo.longitude}?q=${Uri.encode(produtoNaoNulo.enderecoLoja)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.LocationOn, "Endereço", Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ver no Mapa")
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Vendido por", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(produtoNaoNulo.lojaNome, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (produtoNaoNulo.descricao.isNotBlank()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                        Text("Descrição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(produtoNaoNulo.descricao, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}