package com.example.ondetem.ui.screens

import android.content.Context
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
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

private fun copyUriToInternalStorage(context: Context, uri: Uri, fileExtension: String): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = "produto_${System.currentTimeMillis()}.$fileExtension"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Falha ao copiar o arquivo.", Toast.LENGTH_SHORT).show()
        null
    }
}

@Composable
fun CadastroProdutoScreen(
    nomeLoja: String?, // Pode ser nulo no modo de edição
    produtoId: Int?,   // Se não for nulo, estamos em modo de edição
    onProdutoSalvo: () -> Unit
) {
    // --- LÓGICA ATUALIZADA PARA LIDAR COM ADIÇÃO E EDIÇÃO ---
    val context = LocalContext.current
    val isEditMode = produtoId != null

    var produtoCarregado by remember { mutableStateOf<Produto?>(null) }
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var imagemUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    // Efeito que roda uma vez se estiver em modo de edição para carregar os dados
    LaunchedEffect(isEditMode) {
        if (isEditMode && produtoId != null) {
            val produto = ProdutoRepository.getProdutoPorId(context, produtoId)
            if (produto != null) {
                produtoCarregado = produto
                nome = produto.nome
                descricao = produto.descricao
                preco = produto.preco
                if (produto.imagemUrl.isNotBlank()) imagemUri = File(produto.imagemUrl).toUri()
                if (produto.videoUrl.isNotBlank()) videoUri = File(produto.videoUrl).toUri()
            }
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
            text = if (isEditMode) "Editar Produto" else "Adicionar Produto à Loja",
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

        OutlinedButton(onClick = { seletorDeImagem.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (imagemUri == null) "Selecionar Imagem da Galeria" else "Trocar Imagem")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { seletorDeVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (videoUri == null) "Selecionar Vídeo da Galeria" else "Trocar Vídeo")
        }
        if (videoUri != null) Text("Vídeo selecionado!", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (nome.isNotBlank() && descricao.isNotBlank() && preco.isNotBlank()) {
                    // --- LÓGICA DE SALVAMENTO ATUALIZADA ---
                    val caminhoDaImagem = imagemUri?.let { if (!it.toString().startsWith("file://")) copyUriToInternalStorage(context, it, "jpg") else it.path }
                    val caminhoDoVideo = videoUri?.let { if (!it.toString().startsWith("file://")) copyUriToInternalStorage(context, it, "mp4") else it.path }

                    if (isEditMode) { // MODO DE EDIÇÃO
                        val produtoAtualizado = produtoCarregado!!.copy(
                            nome = nome,
                            descricao = descricao,
                            preco = preco,
                            imagemUrl = caminhoDaImagem ?: "",
                            videoUrl = caminhoDoVideo ?: ""
                        )
                        ProdutoRepository.atualizar(context, produtoAtualizado)
                        Toast.makeText(context, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                    } else { // MODO DE ADIÇÃO
                        val loja = LojaRepository.listarTodas(context).firstOrNull { it.nome == nomeLoja }
                        if (loja != null) {
                            val novoProduto = Produto(id = Random.nextInt(), nome = nome, descricao = descricao, preco = preco, loja = nomeLoja!!, endereco = loja.endereco, telefone = loja.telefone, imagemUrl = caminhoDaImagem ?: "", videoUrl = caminhoDoVideo ?: "")
                            ProdutoRepository.salvar(context, novoProduto)
                            Toast.makeText(context, "Produto cadastrado!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    onProdutoSalvo()
                } else {
                    Toast.makeText(context, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditMode) "Salvar Alterações" else "Salvar Produto")
        }
    }
}