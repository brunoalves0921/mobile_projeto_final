package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.VendedorRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSucesso: (String) -> Unit, // Alterado para receber o email do vendedor
    onIrParaCadastro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login do Vendedor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && senha.isNotBlank()) {
                    val emailLimpo = email.trim()
                    val valido = VendedorRepository.validarLogin(context, emailLimpo, senha)
                    scope.launch {
                        if (valido) {
                            snackbarHostState.showSnackbar("Login bem-sucedido!")
                            onLoginSucesso(emailLimpo) // Passa o email para a navegação
                        } else {
                            snackbarHostState.showSnackbar("Credenciais inválidas.")
                        }
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Preencha todos os campos.")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        TextButton(onClick = onIrParaCadastro) {
            Text("Não tem uma conta? Cadastre-se")
        }

        Spacer(Modifier.height(8.dp))
        SnackbarHost(snackbarHostState)
    }
}