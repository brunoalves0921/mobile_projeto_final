package com.example.ondetem.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ondetem.ui.components.ProdutoCard
import com.example.ondetem.viewmodel.ProdutoViewModel

@Composable
fun FavoritosScreen(viewModel: ProdutoViewModel, onItemClick: (Int) -> Unit) {
    LazyColumn {
        items(viewModel.favoritos) { produto ->
            ProdutoCard(produto = produto) {
                onItemClick(produto.id)
            }
        }
    }
}