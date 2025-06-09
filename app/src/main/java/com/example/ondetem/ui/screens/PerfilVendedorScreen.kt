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
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.Vendedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilVendedorScreen(
    onCadastrarLoja: (String) -> Unit,
    onLogout: () -> Unit,
    onLojaClick: (String) -> Unit // Passa o ID da loja
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var vendedor by remember { mutableStateOf<Vendedor?>(null) }
    var lojas by remember { mutableStateOf<List<Loja>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Efeito que busca os dados do Firestore quando a tela é aberta
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onLogout() // Se não há usuário, desloga
            return@LaunchedEffect
        }

        val uid = currentUser.uid

        try {
            // Busca os dados do vendedor no documento com seu UID
            val vendedorDoc = db.collection("vendedores").document(uid).get().await()
            vendedor = vendedorDoc.toObject(Vendedor::class.java)

            // Busca as lojas usando o novo LojaRepository
            lojas = LojaRepository.getLojasPorVendedor(uid)

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
                        auth.signOut()
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
                                    .clickable { onLojaClick(loja.id) }, // Navega usando o ID da loja
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(loja.nome, style = MaterialTheme.typography.titleMedium)
                                    Text(loja.endereco, style = MaterialTheme.typography.bodySmall)
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