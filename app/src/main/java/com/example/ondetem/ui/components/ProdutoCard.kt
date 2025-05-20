package com.example.ondetem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ondetem.data.Produto

@Composable
fun ProdutoCard(produto: Produto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = produto.imagemUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(produto.nome, style = MaterialTheme.typography.titleMedium)
                Text(produto.descricao, style = MaterialTheme.typography.bodyMedium)
                Text(produto.preco, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}