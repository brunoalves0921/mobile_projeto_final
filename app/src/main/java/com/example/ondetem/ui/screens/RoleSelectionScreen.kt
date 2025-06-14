package com.example.ondetem.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun RoleSelectionScreen(onSelecionarCliente: () -> Unit, onSelecionarVendedor: () -> Unit) {
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val context = LocalContext.current

    var isClienteLoading by remember { mutableStateOf(false) }

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
            onClick = {
                isClienteLoading = true
                scope.launch {
                    try {
                        if (auth.currentUser != null) {
                            auth.signOut()
                        }
                        // 1. Faz o login anônimo
                        auth.signInAnonymously().await()

                        // 2. --- CORREÇÃO APLICADA AQUI ---
                        // Após o login, busca e salva o token de notificação (FCM)
                        UserRepository.fetchAndSaveFcmToken()

                        // 3. Navega para a tela principal do cliente
                        onSelecionarCliente()

                    } catch (e: Exception) {
                        Log.e("RoleSelectionScreen", "Falha no login anônimo", e)
                        Toast.makeText(context, "Falha na conexão. Tente novamente.", Toast.LENGTH_SHORT).show()
                    } finally {
                        isClienteLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isClienteLoading
        ) {
            if (isClienteLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sou Cliente")
            }
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