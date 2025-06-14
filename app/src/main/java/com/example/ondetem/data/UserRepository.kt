package com.example.ondetem.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    suspend fun fetchAndSaveFcmToken() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            Log.e("UserRepository", "Usuário não logado, não é possível salvar o token FCM.")
            return
        }
        try {
            val token = Firebase.messaging.token.await()
            saveUserFcmToken(userId, token)
        } catch (e: Exception) {
            Log.e("UserRepository", "Falha ao obter o token FCM.", e)
        }
    }

    fun getNotificationsFlow(userId: String): Flow<List<UserNotification>> = callbackFlow {
        val listener = usersCollection.document(userId)
            .collection("userNotifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.toObjects<UserNotification>()
                    trySend(notifications)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserFcmToken(userId: String, token: String) {
        try {
            val tokenData = mapOf("fcmToken" to token)
            usersCollection.document(userId).set(tokenData, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Erro ao salvar o FCM Token: ${e.message}", e)
        }
    }

    suspend fun deleteNotification(userId: String, notificationId: String) {
        try {
            usersCollection.document(userId)
                .collection("userNotifications")
                .document(notificationId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Erro ao deletar notificação: ${e.message}", e)
        }
    }

    // --- NOVAS FUNÇÕES ADICIONADAS ---

    /**
     * Cria um fluxo que emite a CONTAGEM de notificações não lidas.
     */
    fun getUnreadNotificationsCountFlow(userId: String): Flow<Int> {
        return getNotificationsFlow(userId).map { notifications ->
            notifications.count { !it.isRead }
        }
    }

    /**
     * Marca todas as notificações de um usuário como lidas.
     */
    suspend fun markAllNotificationsAsRead(userId: String) {
        try {
            val notificationsQuery = usersCollection.document(userId)
                .collection("userNotifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (notificationsQuery.isEmpty) return

            val batch: WriteBatch = db.batch()
            notificationsQuery.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Erro ao marcar notificações como lidas: ${e.message}", e)
        }
    }
}