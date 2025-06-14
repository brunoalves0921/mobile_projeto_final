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
    var isVendedorLoading by remember { mutableStateOf(false) } // Adicionado para o botão de vendedor

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

        // --- BOTÃO SOU CLIENTE ---
        Button(
            onClick = {
                isClienteLoading = true
                scope.launch {
                    try {
                        // Só cria um usuário anônimo se não houver um ou se o usuário logado for um vendedor.
                        if (auth.currentUser == null || !auth.currentUser!!.isAnonymous) {
                            if (auth.currentUser != null) {
                                auth.signOut() // Desloga o vendedor antes de logar como cliente
                            }
                            auth.signInAnonymously().await()
                            UserRepository.fetchAndSaveFcmToken()
                        }
                        onSelecionarCliente()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Falha na conexão. Tente novamente.", Toast.LENGTH_SHORT).show()
                    } finally {
                        isClienteLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isClienteLoading && !isVendedorLoading
        ) {
            if (isClienteLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Sou Cliente")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- BOTÃO SOU VENDEDOR ---
        OutlinedButton(
            onClick = {
                isVendedorLoading = true
                scope.launch {
                    // Garante que qualquer usuário anônimo seja deslogado
                    // antes de ir para a tela de login do vendedor.
                    if (auth.currentUser != null && auth.currentUser!!.isAnonymous) {
                        auth.signOut()
                    }
                    onSelecionarVendedor()
                    isVendedorLoading = false // Reseta o estado
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isClienteLoading && !isVendedorLoading
        ) {
            if (isVendedorLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Sou Vendedor")
            }
        }
    }
}