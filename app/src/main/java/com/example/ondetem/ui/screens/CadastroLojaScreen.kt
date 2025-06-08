package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Loja
import com.example.ondetem.data.LojaRepository

@Composable
fun CadastroLojaScreen(
    vendedorEmail: String,
    onLojaCadastrada: () -> Unit
) {
    var nomeLoja by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastrar Nova Loja", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nomeLoja,
            onValueChange = { nomeLoja = it },
            label = { Text("Nome da Loja") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = endereco,
            onValueChange = { endereco = it },
            label = { Text("Endereço Completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = telefone,
            onValueChange = { telefone = it },
            label = { Text("Telefone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (nomeLoja.isNotBlank() && endereco.isNotBlank() && telefone.isNotBlank()) {
                    val novaLoja = Loja(
                        nome = nomeLoja,
                        endereco = endereco,
                        telefone = telefone,
                        donoEmail = vendedorEmail
                    )
                    LojaRepository.salvar(context, novaLoja)
                    Toast.makeText(context, "Loja cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                    onLojaCadastrada()
                } else {
                    Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Loja")
        }
    }
}