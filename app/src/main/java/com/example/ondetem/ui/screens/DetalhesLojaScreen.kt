package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
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

    // Efeito para buscar os produtos da loja
    LaunchedEffect(lojaId) {
        isLoading = true
        produtosDaLoja = ProdutoRepository.getProdutosPorLoja(lojaId)
        isLoading = false
    }

    // Dialog de confirmação para deletar um produto
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
                            produtosDaLoja = ProdutoRepository.getProdutosPorLoja(lojaId) // Atualiza a lista
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Meus Produtos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (produtosDaLoja.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum produto cadastrado nesta loja ainda.")
                }
            } else {
                // --- GRADE DE PRODUTOS ORGANIZADA ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Layout com 2 colunas
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(produtosDaLoja) { produto ->
                        ProdutoGerenciavelCard(
                            produto = produto,
                            onEditClick = { onEditProduto(produto.id) },
                            onDeleteClick = { showDialog = produto }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Novo Card de Produto que inclui botões de Edição e Exclusão.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutoGerenciavelCard(
    produto: Produto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onEditClick // O card inteiro é clicável para edição
    ) {
        Column {
            // Imagem do Produto
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(produto.imagemUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagem de ${produto.nome}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            // Informações do produto
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    // CORREÇÃO: Usar 'precoEmCentavos' e formatar para moeda
                    text = "R$ %.2f".format(produto.precoEmCentavos / 100.0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Divider para separar as informações dos botões
            Divider(modifier = Modifier.padding(horizontal = 8.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, "Deletar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}