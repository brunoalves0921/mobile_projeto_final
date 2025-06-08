package com.example.ondetem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.VendedorRepository

@OptIn(ExperimentalMaterial3Api::class) // Necessário para Card clicável
@Composable
fun PerfilVendedorScreen(
    vendedorEmail: String,
    onCadastrarLoja: () -> Unit,
    onLogout: () -> Unit,
    onLojaClick: (String) -> Unit // NOVO: Ação ao clicar em uma loja
) {
    val context = LocalContext.current
    val vendedor = VendedorRepository.listar(context).firstOrNull { it.email == vendedorEmail }
    val lojas = LojaRepository.getLojasPorVendedor(context, vendedorEmail)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (vendedor != null) {
            Text("Olá, ${vendedor.nome}", style = MaterialTheme.typography.headlineMedium)
            Text(vendedor.email, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onCadastrarLoja, modifier = Modifier.fillMaxWidth()) {
            Text("Cadastrar Nova Loja")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Sair (Logout)")
        }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("Minhas Lojas Cadastradas", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        if (lojas.isEmpty()) {
            Text("Você ainda não cadastrou nenhuma loja.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lojas) { loja ->
                    // Card agora é clicável
                    Card(
                        onClick = { onLojaClick(loja.nome) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(loja.nome, style = MaterialTheme.typography.titleMedium)
                            Text(loja.endereco)
                            Text(loja.telefone)
                        }
                    }
                }
            }
        }
    }
}