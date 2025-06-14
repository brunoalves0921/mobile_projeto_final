package com.example.ondetem.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object VendedorRepository {

    private val db = Firebase.firestore
    private val vendedoresCollection = db.collection("vendedores")

    /**
     * Busca os dados de um vendedor no Firestore usando seu UID.
     */
    suspend fun getVendedor(uid: String): Vendedor? {
        return try {
            vendedoresCollection.document(uid).get().await().toObject(Vendedor::class.java)
        } catch (e: Exception) {
            // Em caso de erro, retorna nulo
            null
        }
    }
}