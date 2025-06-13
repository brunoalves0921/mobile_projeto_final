package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    onItemClick: (String) -> Unit
) {
    if (favoriteProducts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Você ainda não adicionou nenhum favorito.")
        }
    } else {
        // AQUI ESTÁ A MUDANÇA: Usando LazyVerticalGrid em vez de LazyColumn
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = favoriteProducts,
                key = { produto -> produto.id } // Usando a key para performance
            ) { produto ->
                // Reutilizando nosso ProdutoCard já redesenhado
                ProdutoCard(produto = produto) {
                    onItemClick(produto.id)
                }
            }
        }
    }
}