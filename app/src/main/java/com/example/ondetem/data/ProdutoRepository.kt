package com.example.ondetem.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

object ProdutoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val produtosCollection = db.collection("produtos")

    suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val imageUrls = mutableListOf<String>()
        coroutineScope {
            val uploadJobs = imageUris.map { uri ->
                async(Dispatchers.IO) {
                    val storageRef = storage.reference.child("produtos/${UUID.randomUUID()}")
                    try {
                        val uploadTask = storageRef.putFile(uri).await()
                        uploadTask.storage.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            imageUrls.addAll(uploadJobs.awaitAll().filterNotNull())
        }
        return imageUrls
    }

    suspend fun salvarProduto(produto: Produto) {
        if (produto.id.isBlank()) {
            produtosCollection.add(produto).await()
        } else {
            produtosCollection.document(produto.id).set(produto).await()
        }
    }

    suspend fun getProdutoById(produtoId: String): Produto? {
        return try {
            val document = produtosCollection.document(produtoId).get().await()
            document.toObject(Produto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun listarTodosFlow(): Flow<List<Produto>> = callbackFlow {
        val listener = produtosCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val produtos = snapshot.toObjects<Produto>()
                trySend(produtos)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getProdutosPorLojaFlow(lojaId: String): Flow<List<Produto>> = callbackFlow {
        val query = produtosCollection.whereEqualTo("lojaId", lojaId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val produtos = snapshot.toObjects<Produto>()
                trySend(produtos)
            }
        }
        awaitClose { listener.remove() }
    }

    // ================================================================
    // ===== FUNÇÃO ADICIONADA PARA CORRIGIR O ERRO ===================
    // ================================================================
    /**
     * Busca todos os produtos de uma loja específica.
     * Necessária para a função de deletar a loja.
     */
    private suspend fun getProdutosPorLoja(lojaId: String): List<Produto> {
        return try {
            produtosCollection.whereEqualTo("lojaId", lojaId).get().await().toObjects(Produto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Deleta todos os produtos associados a um ID de loja.
     * Esta era a função que estava faltando e causando o erro.
     */
    suspend fun deletarProdutosPorLoja(lojaId: String) {
        val produtosParaDeletar = getProdutosPorLoja(lojaId)
        coroutineScope {
            produtosParaDeletar.map { produto ->
                async(Dispatchers.IO) {
                    deletar(produto) // Reutiliza a função de deletar individual
                }
            }.awaitAll()
        }
    }


    suspend fun deletar(produto: Produto) {
        produtosCollection.document(produto.id).delete().await()

        // Esta lógica já está correta para múltiplas imagens
        produto.imageUrls.forEach { imageUrl ->
            if (imageUrl.isNotBlank()) {
                try {
                    storage.getReferenceFromUrl(imageUrl).delete().await()
                } catch (e: Exception) {
                    println("Falha ao deletar imagem $imageUrl: ${e.message}")
                }
            }
        }

        if (produto.videoUrl.isNotBlank()) {
            try {
                storage.getReferenceFromUrl(produto.videoUrl).delete().await()
            } catch (e: Exception) {
                println("Falha ao deletar vídeo ${produto.videoUrl}: ${e.message}")
            }
        }
    }
}