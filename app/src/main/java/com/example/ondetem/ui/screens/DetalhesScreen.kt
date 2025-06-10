package com.example.ondetem.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ondetem.data.Produto
import com.example.ondetem.notifications.NotificationReceiver
import com.example.ondetem.ui.components.VideoPlayer
import com.example.ondetem.viewmodel.ProdutoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetalhesScreen(
    produtoId: String,
    viewModel: ProdutoViewModel,
    favoriteProducts: List<Produto>,
    onToggleFavorite: (Produto) -> Unit,
    areNotificationsEnabled: Boolean
) {
    val context = LocalContext.current
    var produto by remember { mutableStateOf<Produto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(produtoId) {
        isLoading = true
        produto = viewModel.todosOsProdutos.firstOrNull { it.id == produtoId }
        isLoading = false
    }

    val notificacaoPermissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (produto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Produto não encontrado.")
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ) {
            Text(produto!!.nome, style = MaterialTheme.typography.headlineMedium)
            Text(produto!!.descricao, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Preço: ${produto!!.preco}")
            Text("Loja: ${produto!!.lojaNome}")
            Spacer(modifier = Modifier.height(16.dp))

            if (produto!!.imagemUrl.isNotBlank()) {
                AsyncImage(model = produto!!.imagemUrl, contentDescription = "Imagem do produto", modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (produto!!.videoUrl.isNotBlank()) {
                VideoPlayer(
                    videoUrl = produto!!.videoUrl,
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val isFavorito = favoriteProducts.any { it.id == produto!!.id }
            val escalaIcone by animateFloatAsState(targetValue = if (isFavorito) 1.2f else 1.0f, label = "")

            Button(onClick = { onToggleFavorite(produto!!) }) {
                Icon(imageVector = if (isFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favoritar", tint = if (isFavorito) MaterialTheme.colorScheme.error else LocalContentColor.current, modifier = Modifier.scale(escalaIcone))
                Spacer(Modifier.width(8.dp))
                Text(if (isFavorito) "Remover dos Favoritos" else "Adicionar aos Favoritos")
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = {
                if (!areNotificationsEnabled) {
                    Toast.makeText(context, "As notificações estão desativadas.", Toast.LENGTH_SHORT).show()
                    return@OutlinedButton
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!notificacaoPermissionState.status.isGranted) {
                        notificacaoPermissionState.launchPermissionRequest()
                        return@OutlinedButton
                    }
                }
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("produto_nome", produto!!.nome)
                    putExtra("loja_nome", produto!!.lojaNome)
                }

                val requestCode = produto!!.id.hashCode()

                val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val triggerAtMillis = Calendar.getInstance().timeInMillis + 10_000
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                Toast.makeText(context, "Lembrete criado para daqui a 10 segundos!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Criar Lembrete")
                Spacer(Modifier.width(8.dp))
                Text("Criar Lembrete")
            }
        }
    }
}