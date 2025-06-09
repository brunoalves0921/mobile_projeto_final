package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Loja
import com.example.ondetem.data.LojaRepository
import kotlinx.coroutines.launch

@Composable
fun CadastroLojaScreen(
    vendedorId: String,
    onLojaCadastrada: () -> Unit
) {
    var nomeLoja by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // CoroutineScope para chamar funções suspend

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastrar Nova Loja", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = nomeLoja,onValueChange = { nomeLoja = it },label = { Text("Nome da Loja") },modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = endereco,onValueChange = { endereco = it },label = { Text("Endereço Completo") },modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = telefone,onValueChange = { telefone = it },label = { Text("Telefone para Contato") },modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (nomeLoja.isNotBlank() && endereco.isNotBlank() && telefone.isNotBlank()) {
                    isLoading = true
                    val novaLoja = Loja(
                        nome = nomeLoja,
                        endereco = endereco,
                        telefone = telefone,
                        donoId = vendedorId
                    )

                    scope.launch {
                        try {
                            LojaRepository.salvar(novaLoja)
                            Toast.makeText(context, "Loja cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                            onLojaCadastrada()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao cadastrar loja: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Salvar Loja")
            }
        }
    }
}