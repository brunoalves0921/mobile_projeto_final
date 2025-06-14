package com.example.ondetem.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    // --- MUDANÇA AQUI: NOVO PARÂMETRO PARA O TÍTULO ---
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title, // Usa o novo parâmetro diretamente
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
        actions = actions
    )
}