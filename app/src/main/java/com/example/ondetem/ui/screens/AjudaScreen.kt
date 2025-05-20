package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AjudaScreen() {
    val faqs = listOf(
        "Como buscar um produto?",
        "Como adicionar aos favoritos?",
        "Posso alterar o tema do app?",
        "Como entro em contato com o suporte?"
    )

    var email by remember { mutableStateOf("") }
    var duvida by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Perguntas Frequentes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(faqs.size) { index ->
                Text(text = "❓ ${faqs[index]}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        Text("Fale com o suporte", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Seu e-mail") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = duvida,
            onValueChange = { duvida = it },
            label = { Text("Sua dúvida") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && duvida.isNotBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Mensagem enviada com sucesso!")
                        email = ""
                        duvida = ""
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Preencha todos os campos antes de enviar.")
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Enviar")
        }

        Spacer(Modifier.height(8.dp))
        SnackbarHost(hostState = snackbarHostState)
    }
}
