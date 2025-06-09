package com.example.ondetem.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object LojaRepository {

    // Instância do banco de dados Firestore
    private val db = FirebaseFirestore.getInstance()
    private val lojasCollection = db.collection("lojas")

    /**
     * Busca todas as lojas de um vendedor específico no Firestore.
     * Usa 'await' de Coroutines para trabalhar de forma assíncrona.
     */
    suspend fun getLojasPorVendedor(vendedorId: String): List<Loja> {
        return try {
            val snapshot = lojasCollection
                .whereEqualTo("donoId", vendedorId)
                .get()
                .await()
            snapshot.toObjects(Loja::class.java)
        } catch (e: Exception) {
            // Em caso de erro, retorna uma lista vazia
            emptyList()
        }
    }

    /**
     * Salva uma nova loja no Firestore.
     */
    suspend fun salvar(loja: Loja) {
        lojasCollection.add(loja).await()
    }

    // Funções antigas que usavam arquivos locais podem ser removidas ou comentadas,
    // pois não serão mais utilizadas.
}