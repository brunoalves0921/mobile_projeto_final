package com.example.ondetem.data

import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object LojaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val lojasCollection = db.collection("lojas")

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

    /**
     * NOVA FUNÇÃO: Atualiza os dados de uma loja existente.
     * Esta função estava faltando e causava o erro.
     */
    suspend fun atualizar(loja: Loja) {
        // A loja já deve ter um ID do Firestore para ser atualizada.
        lojasCollection.document(loja.id).set(loja).await()
    }

    /**
     * NOVA FUNÇÃO: Deleta uma loja e todos os seus produtos.
     */
    suspend fun deletar(lojaId: String) {
        // Primeiro, deleta todos os produtos associados a esta loja.
        ProdutoRepository.deletarProdutosPorLoja(lojaId)
        // Depois, deleta o documento da loja.
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