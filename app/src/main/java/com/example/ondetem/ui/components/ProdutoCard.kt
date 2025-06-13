package com.example.ondetem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // AQUI ESTÁ A MUDANÇA: Usando ConstraintLayout para controle total
            ConstraintLayout(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .height(110.dp) // Altura fixa para a área de texto
            ) {
                // Cria referências para cada elemento que vamos posicionar
                val (nome, loja, preco, distancia) = createRefs()

                // Nome do produto (preso ao topo)
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.constrainAs(nome) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                // Nome da loja (logo abaixo do nome do produto)
                Text(
                    text = produto.lojaNome,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.constrainAs(loja) {
                        top.linkTo(nome.bottom, margin = 2.dp)
                        start.linkTo(parent.start)
                    }
                )

                // Preço (preso à parte inferior esquerda)
                Text(
                    text = formatPrice(produto.precoEmCentavos),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.constrainAs(preco) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
                )

                // Distância (presa à parte inferior direita)
                produto.distanciaEmMetros?.let {
                    Text(
                        text = formatarDistancia(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.constrainAs(distancia) {
                            bottom.linkTo(preco.bottom) // Alinha com a base do preço
                            end.linkTo(parent.end)
                        }
                    )
                }
            }
        }
    }
}