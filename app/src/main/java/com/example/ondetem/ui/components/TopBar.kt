package com.example.ondetem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentRoute: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    TopAppBar(
        title = {
            // AQUI ESTÁ A MUDANÇA:
            // Verificamos se a rota atual é "home"
            if (currentRoute == "home") {
                // Se for, exibimos o nome do app com um estilo personalizado
                Text(
                    text = "Onde Tem?",
                    style = MaterialTheme.typography.headlineSmall, // Um estilo de título maior
                    fontWeight = FontWeight.Bold, // Em negrito
                    color = MaterialTheme.colorScheme.primary // Usa a cor primária do tema
                )
            } else {
                // Para todas as outras telas, exibimos o nome da rota formatado
                Text(
                    text = currentRoute.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions = {
            // Vazio
        }
    )
}