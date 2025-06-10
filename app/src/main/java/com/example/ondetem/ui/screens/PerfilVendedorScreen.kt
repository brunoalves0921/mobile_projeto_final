package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    onLojaClick: (String) -> Unit,
    onEditLoja: (String) -> Unit // Nova ação para navegar para a edição
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var vendedor by remember { mutableStateOf<Vendedor?>(null) }
    var lojas by remember { mutableStateOf<List<Loja>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Loja?>(null) }

    // Função para recarregar a lista de lojas do Firestore
    fun refreshLojas() {
        scope.launch {
            isLoading = true
            vendedor?.uid?.let {
                lojas = LojaRepository.getLojasPorVendedor(it)
            }
            isLoading = false
        }
    }

    // Efeito que busca os dados iniciais do Firestore
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onLogout()
            return@LaunchedEffect
        }
        val uid = currentUser.uid
        try {
            val vendedorDoc = FirebaseFirestore.getInstance().collection("vendedores").document(uid).get().await()
            vendedor = vendedorDoc.toObject(Vendedor::class.java)
            refreshLojas() // Carrega as lojas após obter os dados do vendedor
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    // Diálogo de confirmação para deletar a loja
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja deletar a loja '${showDeleteDialog!!.nome}'? Todos os produtos dela também serão removidos permanentemente.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                LojaRepository.deletar(showDeleteDialog!!.id)
                                Toast.makeText(context, "Loja removida!", Toast.LENGTH_SHORT).show()
                                refreshLojas() // Atualiza a lista na tela
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao deletar: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                showDeleteDialog = null
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deletar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (vendedor != null) {
                Text("Bem-vindo(a), ${vendedor!!.nome}!", style = MaterialTheme.typography.headlineMedium)
                Text("E-mail: ${vendedor!!.email}", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))
            Text("Suas Lojas", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (lojas.isEmpty()) {
                Text("Você ainda não cadastrou nenhuma loja.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(lojas) { loja ->
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    loja.nome,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onLojaClick(loja.id) }
                                )
                                Text(loja.endereco, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { onEditLoja(loja.id) }) {
                                        Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Editar")
                                    }
                                    TextButton(onClick = { showDeleteDialog = loja }) {
                                        Icon(Icons.Default.Delete, "Deletar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Deletar", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vendedor?.uid?.let { onCadastrarLoja(it) } },
                enabled = vendedor != null
            ) {
                Text("Cadastrar Nova Loja")
            }
        }
    }
}