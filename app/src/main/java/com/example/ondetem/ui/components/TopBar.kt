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
    currentRoute: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    // --- MUDANÇA: Adicionamos um parâmetro para as ações ---
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            if (currentRoute == "home") {
                Text(
                    text = "Onde Tem?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
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
        // --- MUDANÇA: Usamos o novo parâmetro aqui ---
        actions = actions
    )
}