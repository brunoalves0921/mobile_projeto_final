package com.example.ondetem.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentRoute: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Onde Tem?") },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Configurações") },
                    onClick = {
                        onNavigateTo("config")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Ajuda") },
                    onClick = {
                        onNavigateTo("ajuda")
                        expanded = false
                    }
                )
            }
        }
    )
}
