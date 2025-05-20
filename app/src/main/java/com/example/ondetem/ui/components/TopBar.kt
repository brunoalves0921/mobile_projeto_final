package com.example.ondetem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onNavigateTo: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Onde Tem?") },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Favoritos") },
                    onClick = {
                        onNavigateTo("favoritos")
                        expanded = false
                    }
                )
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
