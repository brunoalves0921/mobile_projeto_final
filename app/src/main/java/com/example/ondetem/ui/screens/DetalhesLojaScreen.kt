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
// IMPORT NECESSÁRIO PARA O NOVO MÉTODO
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    // --- MUDANÇA 1: USAR O FLUXO DE DADOS ---
    // Em vez de buscar a lista uma vez, agora "coletamos" o fluxo.
    // A variável `produtosDaLoja` se tornará um `State` que o Compose observa.
    // Qualquer nova emissão do Flow irá automaticamente recompor a tela.
    // `initialValue = null` nos ajuda a exibir um loading inicial.
    val produtosDaLoja by ProdutoRepository.getProdutosPorLojaFlow(lojaId)
        .collectAsStateWithLifecycle(initialValue = null)

    // Renomeado para não confundir com o carregamento inicial da lista
    var isActionLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf<Produto?>(null) }

    // --- MUDANÇA 2: REMOVER O LaunchedEffect ---
    // Não é mais necessário, pois o Flow cuida de buscar e atualizar os dados.

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
                            isActionLoading = true
                            ProdutoRepository.deletar(showDialog!!)
                            // --- MUDANÇA 3: REMOVER A ATUALIZAÇÃO MANUAL ---
                            // Não precisamos mais da linha "produtosDaLoja = ...",
                            // a atualização agora é automática!
                            showDialog = null
                            isActionLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deletar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAddProduto) { Icon(Icons.Default.Add, "Adicionar") } }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Meus Produtos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // --- MUDANÇA 4: LÓGICA DE EXIBIÇÃO ATUALIZADA ---
            // Usamos uma cópia local para a checagem de nulo
            val produtos = produtosDaLoja

            when {
                // Se `produtos` é nulo, significa que o Flow ainda não emitiu o primeiro valor.
                produtos == null || isActionLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // O Flow emitiu uma lista vazia.
                produtos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum produto cadastrado nesta loja ainda.")
                    }
                }
                // O Flow emitiu uma lista com produtos.
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(produtos) { produto ->
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
}

// O Composable `ProdutoGerenciavelCard` não precisa de nenhuma alteração.
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
        onClick = onEditClick
    ) {
        Column {
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
                    text = "R$ %.2f".format(produto.precoEmCentavos / 100.0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Divider(modifier = Modifier.padding(horizontal = 8.dp))
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