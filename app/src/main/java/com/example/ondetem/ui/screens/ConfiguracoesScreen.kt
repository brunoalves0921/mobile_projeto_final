package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ondetem.viewmodel.ProdutoViewModel

@Composable
fun ConfiguracoesScreen(
    viewModel: ProdutoViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    // ADICIONADO: Parâmetros para o Switch de notificações
    areNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit
) {
    // REMOVIDO: O estado local não é mais necessário
    // var notificacoes by rememberSaveable { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Modo Escuro", modifier = Modifier.weight(1f))
            Switch(checked = darkMode, onCheckedChange = { onToggleDarkMode() })
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notificações", modifier = Modifier.weight(1f))
            // ALTERADO: O Switch agora usa os parâmetros recebidos
            Switch(
                checked = areNotificationsEnabled,
                onCheckedChange = { onToggleNotifications() }
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /* Lógica para limpar favoritos */ }) {
            Text("Limpar Favoritos")
        }
    }
}