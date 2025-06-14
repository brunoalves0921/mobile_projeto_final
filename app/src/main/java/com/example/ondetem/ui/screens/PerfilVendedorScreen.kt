package com.example.ondetem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Loja
import com.example.ondetem.data.LojaRepository
import com.example.ondetem.data.Vendedor
import com.example.ondetem.data.VendedorRepository // <-- IMPORT ADICIONADO
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilVendedorScreen(
    onCadastrarLoja: (String) -> Unit,
    onLogout: () -> Unit,
    onLojaClick: (String) -> Unit,
    onEditLoja: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var vendedor by remember { mutableStateOf<Vendedor?>(null) }
    var lojas by remember { mutableStateOf<List<Loja>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Loja?>(null) }

    fun refreshLojas() {
        scope.launch {
            vendedor?.uid?.let {
                lojas = LojaRepository.getLojasPorVendedor(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onLogout()
            return@LaunchedEffect
        }
        try {
            // --- MUDANÇA AQUI: USA O REPOSITÓRIO ---
            vendedor = VendedorRepository.getVendedor(currentUser.uid)
            refreshLojas()
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja deletar a loja '${showDeleteDialog!!.nome}'? Todos os produtos dela também serão removidos permanentemente.") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        isLoading = true
                        try { LojaRepository.deletar(showDeleteDialog!!.id); refreshLojas() }
                        catch (e: Exception) { /* Tratar erro */ }
                        finally { showDeleteDialog = null; isLoading = false }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Deletar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil de Vendedor") },
                actions = { TextButton(onClick = { auth.signOut(); onLogout() }) { Text("Logout") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vendedor?.uid?.let { onCadastrarLoja(it) } }) {
                Icon(Icons.Default.Add, "Cadastrar Nova Loja")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)) {
                    item {
                        // --- MUDANÇA AQUI: EXIBE O NOME OU O E-MAIL ---
                        val nomeDisplay = if (vendedor?.nome?.isNotBlank() == true) {
                            vendedor?.nome
                        } else {
                            // Se o nome estiver em branco, mostra o e-mail como alternativa
                            auth.currentUser?.email
                        }
                        Text("Bem-vindo(a), ${nomeDisplay ?: ""}!", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(24.dp))
                        Text("Suas Lojas", style = MaterialTheme.typography.titleLarge)
                    }

                    if (lojas.isEmpty()) {
                        item {
                            Text("Você ainda não cadastrou nenhuma loja.", modifier = Modifier.padding(top = 16.dp))
                        }
                    } else {
                        items(lojas) { loja ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth().clickable { onLojaClick(loja.id) },
                                shape = MaterialTheme.shapes.large,
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = "Ícone da Loja",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(loja.nome, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            loja.endereco,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(onClick = { onEditLoja(loja.id) }) {
                                        Icon(Icons.Default.Edit, "Editar Loja")
                                    }
                                    IconButton(onClick = { showDeleteDialog = loja }) {
                                        Icon(Icons.Default.Delete, "Deletar Loja", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}