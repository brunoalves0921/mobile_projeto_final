package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.ProdutoCard

@Composable
fun FavoritosScreen(
    favoriteProducts: List<Produto>,
    onItemClick: (String) -> Unit // <-- CORREÇÃO: MUDADO DE INT PARA STRING
) {
    if (favoriteProducts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Você ainda não adicionou nenhum favorito.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favoriteProducts) { produto ->
                ProdutoCard(produto = produto) {
                    onItemClick(produto.id) // produto.id agora é uma String
                }
            }
        }
    }
}