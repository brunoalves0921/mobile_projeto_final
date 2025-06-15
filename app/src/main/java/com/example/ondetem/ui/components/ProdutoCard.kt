package com.example.ondetem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ondetem.data.Produto
import com.example.ondetem.ui.utils.formatPrice

@Composable
fun ProdutoCard(produto: Produto, onClick: () -> Unit) {

    fun formatarDistancia(metros: Float?): String {
        if (metros == null) return ""
        return if (metros < 1000) {
            "${metros.toInt()} m"
        } else {
            "%.1f km".format(metros / 1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // --- IMAGEM À ESQUERDA ---
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (produto.imagemUrl.isNotBlank()) {
                AsyncImage(
                    model = produto.imagemUrl,
                    contentDescription = "Imagem do produto ${produto.nome}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ImageNotSupported,
                    contentDescription = "Sem imagem",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // ================================================================
        // ===== MUDANÇA NO LAYOUT DA COLUNA DE INFORMAÇÕES ===============
        // ================================================================
        Column(
            // A Column agora ocupa toda a altura disponível da imagem
            modifier = Modifier
                .weight(1f)
                .height(110.dp) // Garante que a altura seja a mesma da imagem
        ) {
            // 1. NOME DO PRODUTO
            Text(
                text = produto.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 2. NOME DA LOJA
            Text(
                text = produto.lojaNome,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 3. SPACER COM PESO (O "TRUQUE")
            // Este Spacer ocupa todo o espaço vertical restante, empurrando o
            // conteúdo abaixo dele para o fundo da Column.
            Spacer(modifier = Modifier.weight(1f))

            // 4. PREÇO E DISTÂNCIA NA MESMA LINHA (AGORA EMBAIXO)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Preço
                Text(
                    text = formatPrice(produto.precoEmCentavos),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Distância (se houver)
                produto.distanciaEmMetros?.let {
                    Text(
                        text = formatarDistancia(it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}