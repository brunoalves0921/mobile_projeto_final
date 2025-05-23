package com.example.ondetem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Vendedor
import com.example.ondetem.data.VendedorRepository
import kotlinx.coroutines.launch

@Composable
fun CadastroScreen(
    onCadastroSucesso: () -> Unit,
    onVoltarParaLogin: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
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
        Text("Cadastro do Vendedor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

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
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = { confirmarSenha = it },
            label = { Text("Confirmar Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val nomeLimpo = nome.trim()
                val emailLimpo = email.trim()
                val senhaLimpa = senha.trim()
                val confirmarSenhaLimpa = confirmarSenha.trim()

                val emailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches()
                val senhaValida = senhaLimpa.length >= 6 && !senhaLimpa.contains(" ")

                if (nomeLimpo.isBlank()) {
                    scope.launch { snackbarHostState.showSnackbar("Informe seu nome.") }
                } else if (!emailValido) {
                    scope.launch { snackbarHostState.showSnackbar("E-mail inválido.") }
                } else if (!senhaValida) {
                    scope.launch { snackbarHostState.showSnackbar("A senha deve ter no mínimo 6 caracteres e não conter espaços.") }
                } else if (senhaLimpa != confirmarSenhaLimpa) {
                    scope.launch { snackbarHostState.showSnackbar("As senhas não coincidem.") }
                } else if (VendedorRepository.emailJaCadastrado(context, emailLimpo)) {
                    scope.launch { snackbarHostState.showSnackbar("E-mail já cadastrado.") }
                } else {
                    VendedorRepository.salvar(context, Vendedor(nomeLimpo, emailLimpo, senhaLimpa))
                    scope.launch {
                        snackbarHostState.showSnackbar("Cadastro realizado com sucesso!")
                        onCadastroSucesso()
                    }
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar")
        }

        TextButton(onClick = onVoltarParaLogin) {
            Text("Já tem uma conta? Fazer login")
        }

        Spacer(Modifier.height(8.dp))
        SnackbarHost(snackbarHostState)
    }
}
