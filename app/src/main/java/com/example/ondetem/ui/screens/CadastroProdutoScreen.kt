package com.example.ondetem.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.ondetem.data.Loja
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import kotlinx.coroutines.launch

@Composable
fun CadastroProdutoScreen(
    lojaId: String?,
    produtoId: String?,
    onProdutoSalvo: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEditMode = produtoId != null

    var produtoCarregado by remember { mutableStateOf<Produto?>(null) }
    // AQUI ESTÁ A CORREÇÃO DO ERRO DE DIGITAÇÃO
    var lojaCarregada by remember { mutableStateOf<Loja?>(null) }
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var imagemUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0.0) }
    var uploadMessage by remember { mutableStateOf("") }

    LaunchedEffect(key1 = produtoId, key2 = lojaId) {
        isSaving = true
        if (isEditMode && produtoId != null) {
            produtoCarregado = ProdutoRepository.getProdutoPorId(produtoId)
            produtoCarregado?.let {
                nome = it.nome; descricao = it.descricao; preco = it.preco
                if (it.imagemUrl.isNotBlank()) imagemUri = it.imagemUrl.toUri()
                if (it.videoUrl.isNotBlank()) videoUri = it.videoUrl.toUri()
            }
        } else if (lojaId != null) {
            lojaCarregada = LojaRepository.getLojaPorId(lojaId)
        }
        isSaving = false
    }

    val seletorDeImagem = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) imagemUri = uri }
    val seletorDeVideo = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) videoUri = uri }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if (isSaving) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uploadMessage, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { (uploadProgress / 100).toFloat() }, modifier = Modifier.fillMaxWidth())
                Text(String.format("%.1f%%", uploadProgress))
            }
        } else {
            Text(text = if (isEditMode) "Editar Produto" else "Adicionar Produto", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 24.dp))
            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = preco, onValueChange = { preco = it }, label = { Text("Preço") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible = imagemUri != null) {
                Column {
                    AsyncImage(model = imagemUri, contentDescription = "Imagem", modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(8.dp))
                }
            }
            OutlinedButton(onClick = { seletorDeImagem.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) { Text("Selecionar Imagem") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { seletorDeVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }, modifier = Modifier.fillMaxWidth()) { Text("Selecionar Vídeo") }
            AnimatedVisibility(visible = videoUri != null) { Text("Vídeo selecionado!", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=8.dp)) }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (nome.isNotBlank() && preco.isNotBlank()) {
                        isSaving = true
                        scope.launch {
                            try {
                                if (isEditMode) {
                                    uploadMessage = "Atualizando produto..."
                                    var imgUrl = produtoCarregado?.imagemUrl ?: ""
                                    if (imagemUri != null && imagemUri.toString() != produtoCarregado?.imagemUrl) imgUrl = ProdutoRepository.uploadMedia(imagemUri!!, "imagens_produtos") { progress -> uploadProgress = progress }
                                    var vidUrl = produtoCarregado?.videoUrl ?: ""
                                    if (videoUri != null && videoUri.toString() != produtoCarregado?.videoUrl) vidUrl = ProdutoRepository.uploadMedia(videoUri!!, "videos_produtos") { progress -> uploadProgress = progress }
                                    val prodAtualizado = produtoCarregado!!.copy(nome=nome, descricao=descricao, preco=preco, imagemUrl=imgUrl, videoUrl=vidUrl)
                                    ProdutoRepository.atualizar(prodAtualizado)
                                    Toast.makeText(context, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                                } else {
                                    uploadMessage = "Criando registo do produto..."
                                    uploadProgress = 0.0
                                    val loja = lojaCarregada!!
                                    var novoProduto = Produto(nome=nome, descricao=descricao, preco=preco, lojaId=loja.id, lojaNome=loja.nome)
                                    val novoId = ProdutoRepository.salvar(novoProduto)
                                    novoProduto = novoProduto.copy(id = novoId)
                                    if(imagemUri != null) {
                                        uploadMessage = "A enviar imagem..."
                                        val url = ProdutoRepository.uploadMedia(imagemUri!!, "imagens_produtos") { progress -> uploadProgress = progress }
                                        novoProduto = novoProduto.copy(imagemUrl = url)
                                    }
                                    if(videoUri != null) {
                                        uploadMessage = "A enviar vídeo (pode demorar)..."
                                        uploadProgress = 0.0
                                        val url = ProdutoRepository.uploadMedia(videoUri!!, "videos_produtos") { progress -> uploadProgress = progress }
                                        novoProduto = novoProduto.copy(videoUrl = url)
                                    }
                                    uploadMessage = "A finalizar..."
                                    ProdutoRepository.atualizar(novoProduto)
                                    Toast.makeText(context, "Produto criado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                                onProdutoSalvo()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Salvar Alterações" else "Salvar Produto")
            }
        }
    }
}