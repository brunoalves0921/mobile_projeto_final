package com.example.ondetem.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.produtosMockados
import com.example.ondetem.viewmodel.ProdutoViewModel

@Composable
fun DetalhesScreen(produtoId: Int, viewModel: ProdutoViewModel) {
    val produto = produtosMockados.firstOrNull { it.id == produtoId } ?: return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(produto.nome, style = MaterialTheme.typography.headlineMedium)
        Text(produto.descricao, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Preço: ${produto.preco}")
        Text("Loja: ${produto.loja}")
        Text("Endereço: ${produto.endereco}")
        Text("Telefone: ${produto.telefone}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.alternarFavorito(produto) }) {
            Text(if (viewModel.favoritos.contains(produto)) "Remover dos Favoritos" else "Adicionar aos Favoritos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exemplo de vídeo (mockado)
        if (produto.videoUrl.isNotBlank()) {
            val context = LocalContext.current
            AndroidView(
                factory = {
                    VideoView(context).apply {
                        setVideoURI(Uri.parse(produto.videoUrl))
                        setMediaController(MediaController(context))
                        start()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}