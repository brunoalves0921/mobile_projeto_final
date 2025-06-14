package com.example.ondetem.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.* // <<-- MUDANÇA IMPORTANTE NO IMPORT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ondetem.data.UserNotification
import com.example.ondetem.data.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class) // <-- MUDANÇA IMPORTANTE NA ANOTAÇÃO
@Composable
fun NotificacoesScreen() {
    val userId = Firebase.auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    val notificacoesState by remember(userId) {
        if (userId != null) {
            UserRepository.getNotificationsFlow(userId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = null)

    val notificacoes = notificacoesState

    when {
        notificacoes == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        notificacoes.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Seu histórico de alertas está vazio.")
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notificacoes,
                    key = { it.id }
                ) { notificacao ->

                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                                userId?.let { uid ->
                                    scope.launch {
                                        UserRepository.deleteNotification(uid, notificacao.id)
                                    }
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.targetValue != DismissValue.Default) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                                label = "background color"
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Deletar",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        },
                        directions = setOf(DismissDirection.EndToStart) // Permite deslizar só para a esquerda
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(notificacao.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(notificacao.body, style = MaterialTheme.typography.bodyMedium)
                                notificacao.timestamp?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(it),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}