package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import com.example.ondetem.ui.components.ProdutoCard
import kotlinx.coroutines.launch

@Composable
fun DetalhesLojaScreen(
    lojaId: String,
    onAddProduto: () -> Unit,
    onEditProduto: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var produtosDaLoja by remember { mutableStateOf<List<Produto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf<Produto?>(null) }

    LaunchedEffect(lojaId) {
        isLoading = true
        produtosDaLoja = ProdutoRepository.getProdutosPorLoja(lojaId)
        isLoading = false
    }

    if (showDialog != null) {
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente deletar o produto '${showDialog?.nome}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            ProdutoRepository.deletar(showDialog!!)
                            // Atualiza a lista na tela
                            produtosDaLoja = ProdutoRepository.getProdutosPorLoja(lojaId)
                            showDialog = null
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deletar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduto) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Produtos da Loja", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            if (produtosDaLoja.isEmpty() && !isLoading) {
                Text("Nenhum produto cadastrado nesta loja ainda.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(produtosDaLoja) { produto ->
                        Column {
                            ProdutoCard(produto = produto, onClick = { /* Ação de clique para cliente */ })
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onEditProduto(produto.id) }) {
                                    Icon(Icons.Default.Edit, "Editar", Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Editar")
                                }
                                TextButton(onClick = { showDialog = produto }) {
                                    Icon(Icons.Default.Delete, "Deletar", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(4.dp)); Text("Deletar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}