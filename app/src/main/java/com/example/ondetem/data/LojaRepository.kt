package com.example.ondetem.data

import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object LojaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val lojasCollection = db.collection("lojas")

    // --- NOVA FUNÇÃO REATIVA ---
    /**
     * Cria um fluxo (Flow) que escuta as mudanças em TODAS as lojas em tempo real.
     */
    fun getTodasAsLojasFlow(): Flow<List<Loja>> = callbackFlow {
        val listener = lojasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val lojas = snapshot.toObjects<Loja>()
                trySend(lojas)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getLojasPorVendedor(vendedorId: String): List<Loja> {
        return try {
            val snapshot = lojasCollection.whereEqualTo("donoId", vendedorId).get().await()
            snapshot.toObjects(Loja::class.java)
        } catch (e: Exception) { emptyList() }
    }
    suspend fun getLojaPorId(lojaId: String): Loja? {
        return try {
            lojasCollection.document(lojaId).get().await().toObject(Loja::class.java)
        } catch (e: Exception) { null }
    }
    suspend fun salvar(loja: Loja) {
        lojasCollection.add(loja).await()
    }
    suspend fun atualizar(loja: Loja) {
        lojasCollection.document(loja.id).set(loja).await()
    }
    suspend fun deletar(lojaId: String) {
        ProdutoRepository.deletarProdutosPorLoja(lojaId)
        lojasCollection.document(lojaId).delete().await()
    }
    fun buscarSugestoesDeEndereco(client: PlacesClient, query: String, token: AutocompleteSessionToken): Task<List<AutocompletePrediction>> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountry("BR").setSessionToken(token).setQuery(query).build()
        return client.findAutocompletePredictions(request).continueWith { task ->
            if (task.isSuccessful) {
                task.result.autocompletePredictions
            } else {
                throw task.exception ?: Exception("Erro ao buscar sugestões")
            }
        }
    }
    fun buscarDetalhesDoLocal(client: PlacesClient, placeId: String): Task<Place> {
        val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        return client.fetchPlace(request).continueWith { task ->
            if (task.isSuccessful) {
                task.result.place
            } else {
                throw task.exception ?: Exception("Erro ao buscar detalhes do local")
            }
        }
    }
}