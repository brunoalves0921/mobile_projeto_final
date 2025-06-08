package com.example.ondetem.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ondetem.data.Produto
import com.example.ondetem.data.produtosMockados
import com.example.ondetem.notifications.NotificationReceiver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetalhesScreen(
    produtoId: Int,
    favoriteProducts: List<Produto>,
    onToggleFavorite: (Produto) -> Unit,
    areNotificationsEnabled: Boolean
) {
    val context = LocalContext.current
    val produto = produtosMockados.firstOrNull { it.id == produtoId } ?: return

    val notificacaoPermissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(500)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // RESTAURADO: As informações do produto que estavam faltando
            Text(produto.nome, style = MaterialTheme.typography.headlineMedium)
            Text(produto.descricao, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Preço: ${produto.preco}")
            Text("Loja: ${produto.loja}")
            Text("Endereço: ${produto.endereco}")
            Text("Telefone: ${produto.telefone}")

            Spacer(modifier = Modifier.height(16.dp))

            val isFavorito = favoriteProducts.any { it.id == produto.id }
            val escalaIcone by animateFloatAsState(
                targetValue = if (isFavorito) 1.2f else 1.0f,
                label = "escalaIcone"
            )

            // Botão de favoritar (agora deve aparecer corretamente)
            Button(onClick = { onToggleFavorite(produto) }) {
                Icon(
                    imageVector = if (isFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoritar",
                    tint = if (isFavorito) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    modifier = Modifier.scale(escalaIcone)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isFavorito) "Remover dos Favoritos" else "Adicionar aos Favoritos")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão de criar lembrete
            OutlinedButton(onClick = {
                if (!areNotificationsEnabled) {
                    Toast.makeText(context, "As notificações estão desativadas. Ative nas configurações.", Toast.LENGTH_SHORT).show()
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
                    putExtra("produto_nome", produto.nome)
                    putExtra("loja_nome", produto.loja)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    produto.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
                )

                val triggerAtMillis = Calendar.getInstance().timeInMillis + 10_000

                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)

                Toast.makeText(context, "Lembrete criado para daqui a 10 segundos!", Toast.LENGTH_SHORT).show()

            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Criar Lembrete")
                Spacer(Modifier.width(8.dp))
                Text("Criar Lembrete")
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Seção do vídeo
            if (produto.videoUrl.isNotBlank()) {
                val videoContext = LocalContext.current
                AndroidView(
                    factory = {
                        VideoView(videoContext).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                            setVideoPath(produto.videoUrl)
                            setMediaController(MediaController(videoContext).apply { setAnchorView(this@apply) })
                            setOnPreparedListener { mp ->
                                mp.setOnVideoSizeChangedListener { _, _, _ -> requestLayout() }
                                start()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }
    }
}