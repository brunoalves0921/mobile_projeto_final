package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.ProdutoRepository
import com.example.ondetem.ui.components.ProdutoCard

@Composable
fun DetalhesLojaScreen(
    nomeLoja: String,
    onAddProduto: () -> Unit
) {
    val context = LocalContext.current
    val loja = LojaRepository.getLojasPorVendedor(context, "").firstOrNull { it.nome == nomeLoja }
    val produtosDaLoja = ProdutoRepository.getProdutosPorLoja(context, nomeLoja)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduto) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loja != null) {
                Text(loja.nome, style = MaterialTheme.typography.headlineMedium)
                Text(loja.endereco, style = MaterialTheme.typography.bodyLarge)
                Text(loja.telefone, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
            Text("Produtos da Loja", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            if (produtosDaLoja.isEmpty()) {
                Text("Nenhum produto cadastrado nesta loja ainda.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(produtosDaLoja) { produto ->
                        ProdutoCard(produto = produto, onClick = { /* Não faz nada por enquanto */ })
                    }
                }
            }
        }
    }
}