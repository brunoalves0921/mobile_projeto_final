package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Loja
import com.example.ondetem.data.Vendedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilVendedorScreen(
    onCadastrarLoja: (String) -> Unit, // Passa o ID do vendedor
    onLogout: () -> Unit,
    onLojaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Estados para guardar os dados do vendedor e suas lojas
    var vendedor by remember { mutableStateOf<Vendedor?>(null) }
    var lojas by remember { mutableStateOf<List<Loja>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Efeito para buscar os dados do Firestore quando a tela é aberta
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Se por algum motivo não houver usuário logado, volta para a tela de login
            onLogout()
            return@LaunchedEffect
        }

        val uid = currentUser.uid

        // Busca os dados do vendedor
        try {
            val vendedorDoc = db.collection("vendedores").document(uid).get().await()
            vendedor = vendedorDoc.toObject(Vendedor::class.java)

            // Busca as lojas deste vendedor
            val lojasSnapshot = db.collection("lojas").whereEqualTo("donoId", uid).get().await()
            lojas = lojasSnapshot.toObjects(Loja::class.java)

        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao buscar dados: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil do Vendedor") },
                actions = {
                    TextButton(onClick = {
                        auth.signOut() // Realiza o logout no Firebase
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Bem-vindo(a), ${vendedor?.nome ?: "Vendedor"}!", style = MaterialTheme.typography.headlineMedium)
                Text("E-mail: ${vendedor?.email ?: "Não encontrado"}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(16.dp))
                Text("Suas Lojas", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                if (lojas.isEmpty()) {
                    Text("Você ainda não cadastrou nenhuma loja.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(lojas) { loja ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLojaClick(loja.nome) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(loja.nome, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    vendedor?.uid?.let { onCadastrarLoja(it) }
                }) {
                    Text("Cadastrar Nova Loja")
                }
            }
        }
    }
}