package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.viewmodel.ProdutoViewModel

@Composable
fun ConfiguracoesScreen(
    viewModel: ProdutoViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    areNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    onClearFavorites: () -> Unit
) {
    // 1. Pegamos o contexto atual, necessário para mostrar o Toast.
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Opção Dark Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modo Escuro", modifier = Modifier.weight(1f))
            Switch(
                checked = darkMode,
                onCheckedChange = { onToggleDarkMode() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opção de Notificações
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Receber Notificações", modifier = Modifier.weight(1f))
            Switch(
                checked = areNotificationsEnabled,
                onCheckedChange = { onToggleNotifications() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botão para limpar os favoritos
        Button(
            // 2. Modificamos o onClick para mostrar a mensagem
            onClick = {
                // Primeiro, executa a ação de limpar os favoritos
                onClearFavorites()
                // Depois, mostra a mensagem de confirmação
                Toast.makeText(context, "Favoritos limpos com sucesso!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Limpar Favoritos")
        }
    }
}