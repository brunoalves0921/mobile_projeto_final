package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.components.ProdutoCard

@Composable
fun FavoritosScreen(
    favoriteProducts: List<Produto>,
    onItemClick: (String) -> Unit
) {
    // A tela agora não tem Scaffold, pois o MainScreen já provê isso.
    if (favoriteProducts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Você ainda não tem produtos favoritos.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Usamos a lista com o novo ProdutoCard e a divisória, como combinado.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(
                items = favoriteProducts,
                key = { _, produto -> produto.id }
            ) { index, produto ->
                ProdutoCard(produto = produto) {
                    onItemClick(produto.id)
                }

                if (index < favoriteProducts.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}