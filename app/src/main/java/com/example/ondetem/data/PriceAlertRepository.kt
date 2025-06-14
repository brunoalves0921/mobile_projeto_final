package com.example.ondetem.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object PriceAlertRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val alertsCollection = db.collection("priceAlerts")

    suspend fun salvarAlerta(produto: Produto, desiredPriceInCents: Long) {
        val userId = auth.currentUser?.uid

        // --- LOG DE DEPURAÇÃO 1 ---
        // Vamos verificar se o ID do usuário está sendo pego corretamente.
        if (userId == null) {
            Log.e("PriceAlertRepo", "Erro: Usuário não está logado. Impossível salvar o alerta.")
            return
        }
        Log.d("PriceAlertRepo", "Tentando salvar alerta para o usuário: $userId")

        val novoAlerta = PriceAlert(
            userId = userId,
            productId = produto.id,
            produtoNome = produto.nome,
            desiredPriceInCents = desiredPriceInCents
        )

        try {
            // --- LOG DE DEPURAÇÃO 2 ---
            // Vamos confirmar que a operação de salvar foi chamada.
            Log.d("PriceAlertRepo", "Enviando para o Firestore: $novoAlerta")
            alertsCollection.add(novoAlerta).await()
            Log.d("PriceAlertRepo", "Sucesso: Alerta salvo no Firestore!")
        } catch (e: Exception) {
            // --- LOG DE DEPURAÇÃO 3 ---
            // Se houver qualquer erro, ele será impresso aqui.
            Log.e("PriceAlertRepo", "Falha ao salvar o alerta no Firestore: ${e.message}", e)
        }
    }
}