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
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.ProdutoRepository
import com.example.ondetem.ui.components.ProdutoCard

@Composable
fun DetalhesLojaScreen(
    nomeLoja: String,
    onAddProduto: () -> Unit,
    onEditProduto: (Int) -> Unit // Nova ação para editar
) {
    val context = LocalContext.current
    val loja = LojaRepository.listarTodas(context).firstOrNull { it.nome == nomeLoja }

    // --- LÓGICA ATUALIZADA PARA PERMITIR ATUALIZAÇÃO DA LISTA ---
    var produtosDaLoja by remember { mutableStateOf(ProdutoRepository.getProdutosPorLoja(context, nomeLoja)) }
    var showDialog by remember { mutableStateOf<Int?>(null) } // Guarda o ID do produto a ser deletado

    // Diálogo de confirmação para deletar
    if (showDialog != null) {
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Você tem certeza que deseja deletar este produto? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        ProdutoRepository.deletar(context, showDialog!!)
                        // Atualiza a lista na tela após deletar
                        produtosDaLoja = ProdutoRepository.getProdutosPorLoja(context, nomeLoja)
                        showDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deletar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduto) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(produtosDaLoja) { produto ->
                        Column {
                            ProdutoCard(produto = produto, onClick = { /* Não faz nada */ })
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onEditProduto(produto.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Editar")
                                }
                                TextButton(onClick = { showDialog = produto.id }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deletar", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Deletar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}