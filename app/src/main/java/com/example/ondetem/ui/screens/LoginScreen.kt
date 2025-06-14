package com.example.ondetem.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    onLoginSucesso: () -> Unit,
    onIrParaCadastro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    // --- LÓGICA DE LOGIN AUTOMÁTICO RESTAURADA E CORRIGIDA ---
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        // A verificação `!currentUser.isAnonymous` garante que isso só aconteça para vendedores
        if (currentUser != null && !currentUser.isAnonymous) {
            Toast.makeText(context, "Bem-vindo de volta!", Toast.LENGTH_SHORT).show()
            onLoginSucesso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login do Vendedor", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && senha.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            try {
                                auth.signInWithEmailAndPassword(email.trim(), senha.trim()).await()
                                Log.d("LoginScreen", "Login do vendedor bem-sucedido, salvando token FCM...")
                                UserRepository.fetchAndSaveFcmToken()
                                Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                onLoginSucesso()
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Falha no login", e)
                                Toast.makeText(context, "Falha no login: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Por favor, preencha e-mail e senha.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }
            TextButton(
                onClick = onIrParaCadastro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Não tem uma conta? Cadastre-se")
            }
        }
    }
}