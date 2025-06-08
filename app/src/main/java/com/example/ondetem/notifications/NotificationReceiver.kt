package com.example.ondetem.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ondetem.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val produtoNome = intent.getStringExtra("produto_nome") ?: "um produto"
        val lojaNome = intent.getStringExtra("loja_nome") ?: "uma loja"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lembrete_produto_channel"
        val channelName = "Lembretes de Produtos"

        // Cria o canal de notificação (necessário para Android 8.0+)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Canal para lembretes de produtos salvos."
        }
        notificationManager.createNotificationChannel(channel)

        // Cria a notificação
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone seu. Este é o padrão.
            .setContentTitle("Lembrete: Onde Tem?")
            .setContentText("Não se esqueça do produto '$produtoNome' na loja '$lojaNome'!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // A notificação some ao ser tocada
            .build()

        // Exibe a notificação
        // O ID (primeiro parâmetro) deve ser único para cada notificação que você queira que seja distinta
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}