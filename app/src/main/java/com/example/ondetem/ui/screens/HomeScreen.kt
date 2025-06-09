package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ondetem.ui.components.ProdutoCard
import com.example.ondetem.viewmodel.ProdutoViewModel

@Composable
fun HomeScreen(viewModel: ProdutoViewModel, onItemClick: (Int) -> Unit) {

    // NOVO: LaunchedEffect para recarregar os produtos sempre que a tela for exibida.
    // A chave `true` garante que ele rode apenas uma vez quando a tela entra na composição.
    LaunchedEffect(true) {
        viewModel.carregarTodosOsProdutos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.busca,
            onValueChange = { viewModel.buscar(it) },
            label = { Text("Buscar produto...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { /* Fechar teclado se quiser */ })
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Alterado para mostrar uma mensagem diferente se a busca não retornar nada
            if (viewModel.produtos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (viewModel.busca.isNotBlank()) "Nenhum produto encontrado." else "Digite algo para buscar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(viewModel.produtos) { produto ->
                        ProdutoCard(produto = produto) {
                            onItemClick(produto.id)
                        }
                    }
                }
            }
        }
    }
}