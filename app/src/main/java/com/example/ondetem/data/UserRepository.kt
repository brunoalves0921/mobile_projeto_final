package com.example.ondetem.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    // --- NOVA FUNÇÃO ---
    /**
     * Obtém o token FCM atual do dispositivo e o salva no Firestore
     * para o usuário atualmente logado.
     */
    suspend fun fetchAndSaveFcmToken() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            Log.e("UserRepository", "Usuário não logado, não é possível salvar o token FCM.")
            return
        }

        try {
            val token = Firebase.messaging.token.await()
            Log.d("UserRepository", "Token FCM obtido: $token")
            saveUserFcmToken(userId, token)
        } catch (e: Exception) {
            Log.e("UserRepository", "Falha ao obter o token FCM.", e)
        }
    }
    // Dentro de UserRepository.kt

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
    /**
     * Salva ou atualiza o token de notificação (FCM Token) de um usuário no Firestore.
     */
    suspend fun saveUserFcmToken(userId: String, token: String) {
        try {
            val tokenData = mapOf("fcmToken" to token)
            usersCollection.document(userId).set(tokenData, com.google.firebase.firestore.SetOptions.merge()).await()
            Log.d("UserRepository", "FCM Token salvo com sucesso para o usuário $userId")
        } catch (e: Exception) {
            Log.e("UserRepository", "Erro ao salvar o FCM Token: ${e.message}", e)
        }
    }
}

