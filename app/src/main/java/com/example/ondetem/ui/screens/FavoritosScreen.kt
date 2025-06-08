package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.ProdutoCard

@Composable
fun FavoritosScreen(
    favoriteProducts: List<Produto>,
    onItemClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favoriteProducts) { produto ->
            ProdutoCard(produto = produto) {
                onItemClick(produto.id)
            }
        }
    }
}