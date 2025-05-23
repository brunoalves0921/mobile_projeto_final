package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectionScreen(onSelecionarCliente: () -> Unit, onSelecionarVendedor: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bem-vindo ao Onde Tem?",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSelecionarCliente,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sou Cliente")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onSelecionarVendedor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sou Vendedor")
        }
    }
}
