package com.example.ondetem.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object LojaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val lojasCollection = db.collection("lojas")

    suspend fun getLojasPorVendedor(vendedorId: String): List<Loja> {
        return try {
            val snapshot = lojasCollection
                .whereEqualTo("donoId", vendedorId)
                .get()
                .await()
            snapshot.toObjects(Loja::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Busca uma loja única pelo seu ID de documento.
     * Esta é a função que o erro diz não encontrar.
     */
    suspend fun getLojaPorId(lojaId: String): Loja? {
        return try {
            lojasCollection.document(lojaId).get().await().toObject(Loja::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvar(loja: Loja) {
        lojasCollection.add(loja).await()
    }
}