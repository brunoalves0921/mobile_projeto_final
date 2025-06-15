package com.example.ondetem.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ondetem.ui.utils.CurrencyVisualTransformation
import com.example.ondetem.viewmodel.ProdutoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroProdutoScreen(
    lojaId: String?,
    produtoId: String?,
    onProdutoSalvo: () -> Unit,
    viewModel: ProdutoViewModel = viewModel()
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }

    val existingImageUrls = remember { mutableStateListOf<String>() }
    val newImageUris = remember { mutableStateListOf<Uri>() }
    var primaryImage by remember { mutableStateOf<Any?>(null) }

    var videoInputMethod by remember { mutableStateOf("Arquivo") }
    val videoInputOptions = listOf("Arquivo", "URL")
    var localVideoUri by remember { mutableStateOf<Uri?>(null) }
    var remoteVideoUrl by remember { mutableStateOf("") }

    var finalLojaId by remember { mutableStateOf(lojaId) }

    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            newImageUris.addAll(uris)
            if (primaryImage == null && uris.isNotEmpty()) { primaryImage = uris.first() }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            localVideoUri = uri
            if (uri != null) remoteVideoUrl = ""
        }
    )

    LaunchedEffect(produtoId) {
        if (produtoId != null) {
            viewModel.getProdutoById(produtoId) { produto ->
                if (produto != null) {
                    nome = produto.nome
                    descricao = produto.descricao
                    preco = produto.precoEmCentavos.toString()
                    existingImageUrls.addAll(produto.imageUrls)
                    if (produto.primaryImageUrl.isNotBlank()) primaryImage = produto.primaryImageUrl
                    if (produto.videoUrl.isNotBlank()) remoteVideoUrl = produto.videoUrl

                    finalLojaId = produto.lojaId
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (produtoId == null) "Novo Produto" else "Editar Produto") },
                navigationIcon = {
                    IconButton(onClick = onProdutoSalvo) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    if (nome.isBlank() || preco.isBlank()) {
                        Toast.makeText(context, "Nome e preço são obrigatórios.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (primaryImage == null && (existingImageUrls.isNotEmpty() || newImageUris.isNotEmpty())) {
                        Toast.makeText(context, "Por favor, selecione uma imagem principal.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (finalLojaId == null) {
                        Toast.makeText(context, "Erro: ID da loja não encontrado.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.salvarProdutoCompleto(
                        produtoId = produtoId,
                        nome = nome,
                        descricao = descricao,
                        preco = preco,
                        lojaId = finalLojaId!!,
                        existingImageUrls = existingImageUrls,
                        newImageUris = newImageUris,
                        primaryImage = primaryImage,
                        localVideoUri = localVideoUri,
                        remoteVideoUrl = remoteVideoUrl,
                        onSuccess = onProdutoSalvo
                    )
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Salvar Produto")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Imagens do Produto", style = MaterialTheme.typography.titleMedium) }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Box(
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { multipleImagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.AddAPhoto, "Adicionar Imagem", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    items(existingImageUrls) { url ->
                        ImageThumbnail(model = url, isPrimary = primaryImage == url, onSetPrimary = { primaryImage = url }, onRemove = { existingImageUrls.remove(url); if (primaryImage == url) primaryImage = null })
                    }
                    items(newImageUris) { uri ->
                        ImageThumbnail(model = uri, isPrimary = primaryImage == uri, onSetPrimary = { primaryImage = uri }, onRemove = { newImageUris.remove(uri); if (primaryImage == uri) primaryImage = null })
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Vídeo do Produto (Opcional)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // ================================================================
                // ===== SOLUÇÃO DEFINITIVA: SELETOR CUSTOMIZADO ==================
                // ================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    videoInputOptions.forEach { label ->
                        val isSelected = videoInputMethod == label
                        // Usamos um OutlinedButton para o não selecionado e Button para o selecionado
                        if (isSelected) {
                            Button(
                                onClick = { videoInputMethod = label },
                                modifier = Modifier.weight(1f)
                            ) { Text(label) }
                        } else {
                            OutlinedButton(
                                onClick = { videoInputMethod = label },
                                modifier = Modifier.weight(1f)
                            ) { Text(label) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (videoInputMethod) {
                    "Arquivo" -> {
                        Button(onClick = { videoPickerLauncher.launch("video/*") }) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Selecionar Vídeo")
                        }
                        if (localVideoUri != null) {
                            Text("Vídeo selecionado. Pronto para upload.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    "URL" -> {
                        OutlinedTextField(
                            value = remoteVideoUrl,
                            onValueChange = {
                                remoteVideoUrl = it
                                if (it.isNotEmpty()) localVideoUri = null
                            },
                            label = { Text("URL do Vídeo (Ex: YouTube)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                        )
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Produto") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            }
            item {
                OutlinedTextField(value = preco, onValueChange = { val newText = it.filter { char -> char.isDigit() }; if (newText.length <= 10) { preco = newText } }, label = { Text("Preço") }, modifier = Modifier.fillMaxWidth(), visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}

@Composable
fun ImageThumbnail(model: Any, isPrimary: Boolean, onSetPrimary: () -> Unit, onRemove: () -> Unit) {
    val borderColor = if (isPrimary) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).border(2.dp, borderColor, RoundedCornerShape(8.dp)).clickable(onClick = onSetPrimary)) {
        AsyncImage(model = model, contentDescription = "Miniatura da imagem", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        if (isPrimary) {
            Icon(imageVector = Icons.Default.Star, contentDescription = "Imagem Principal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.TopStart).padding(4.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape).padding(2.dp))
        }
        IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Remover Imagem", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}