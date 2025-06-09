package com.example.ondetem.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CadastroProdutoScreen(
    lojaId: String?,   // Recebe o ID da loja para novos produtos
    produtoId: String?, // Recebe o ID do produto para edição
    onProdutoSalvo: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEditMode = produtoId != null

    // Estados dos campos do formulário
    var produtoCarregado by remember { mutableStateOf<Produto?>(null) }
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var lojaNome by remember { mutableStateOf("") }

    // Estados para as URIs das mídias
    var imagemUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(isEditMode) } // Mostra loading se estiver em modo de edição

    // Carrega os dados do produto se estiver em modo de edição
    LaunchedEffect(produtoId) {
        if (isEditMode && produtoId != null) {
            produtoCarregado = ProdutoRepository.getProdutoPorId(produtoId)
            produtoCarregado?.let {
                nome = it.nome
                descricao = it.descricao
                preco = it.preco
                lojaNome = it.lojaNome
                if (it.imagemUrl.isNotBlank()) imagemUri = it.imagemUrl.toUri()
                if (it.videoUrl.isNotBlank()) videoUri = it.videoUrl.toUri()
            }
            isLoading = false
        }
    }

    val seletorDeImagem = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imagemUri = uri }
    )
    val seletorDeVideo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) videoUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditMode) "Editar Produto" else "Adicionar Produto",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Produto") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = preco, onValueChange = { preco = it }, label = { Text("Preço (Ex: R$ 19,99)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        if (imagemUri != null) {
            AsyncImage(model = imagemUri, contentDescription = "Imagem do produto", modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(onClick = { seletorDeImagem.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) { Text(if (imagemUri == null) "Selecionar Imagem" else "Trocar Imagem") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { seletorDeVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }, modifier = Modifier.fillMaxWidth()) { Text(if (videoUri == null) "Selecionar Vídeo" else "Trocar Vídeo") }
        if (videoUri != null) Text("Vídeo selecionado!", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (nome.isNotBlank() && descricao.isNotBlank() && preco.isNotBlank()) {
                    isLoading = true
                    scope.launch {
                        try {
                            var imagemUrlFinal = produtoCarregado?.imagemUrl ?: ""
                            var videoUrlFinal = produtoCarregado?.videoUrl ?: ""

                            // Faz upload da nova imagem se uma foi selecionada
                            if (imagemUri != null && imagemUri.toString() != produtoCarregado?.imagemUrl) {
                                imagemUrlFinal = ProdutoRepository.uploadMedia(imagemUri!!, "imagens_produtos")
                            }
                            // Faz upload do novo vídeo se um foi selecionado
                            if (videoUri != null && videoUri.toString() != produtoCarregado?.videoUrl) {
                                videoUrlFinal = ProdutoRepository.uploadMedia(videoUri!!, "videos_produtos")
                            }

                            if (isEditMode) { // MODO DE EDIÇÃO
                                val produtoAtualizado = produtoCarregado!!.copy(
                                    nome = nome, descricao = descricao, preco = preco,
                                    imagemUrl = imagemUrlFinal, videoUrl = videoUrlFinal
                                )
                                ProdutoRepository.atualizar(produtoAtualizado)
                                Toast.makeText(context, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                            } else { // MODO DE ADIÇÃO
                                val novoProduto = Produto(
                                    nome = nome, descricao = descricao, preco = preco,
                                    lojaId = lojaId!!, lojaNome = lojaNome, // Assumindo que lojaNome seria buscado
                                    imagemUrl = imagemUrlFinal, videoUrl = videoUrlFinal
                                )
                                ProdutoRepository.salvar(novoProduto)
                                Toast.makeText(context, "Produto cadastrado!", Toast.LENGTH_SHORT).show()
                            }
                            onProdutoSalvo()
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
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text(if (isEditMode) "Salvar Alterações" else "Salvar Produto")
        }
    }
}