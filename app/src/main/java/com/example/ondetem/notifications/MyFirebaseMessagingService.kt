package com.example.ondetem.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ondetem.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Esta função é chamada quando uma nova notificação push é recebida
    // enquanto o app está em primeiro ou segundo plano.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")

        // A notificação tem um título e um corpo?
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        }
    }

    // Esta função é chamada quando um novo token de dispositivo é gerado.
    // Você precisaria salvar este token no Firestore junto com o usuário
    // para enviar notificações para dispositivos específicos.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Aqui você enviaria o token para o seu servidor/Firestore
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "price_drop_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação (necessário para Android 8.0+)
        val channel = NotificationChannel(
            channelId,
            "Alertas de Queda de Preço",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone seu
            .setContentTitle(title ?: "Onde Tem?")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}