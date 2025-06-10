package com.example.ondetem.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

object ProdutoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val produtosCollection = db.collection("produtos")

    // ... (funções existentes: listarTodos, getProdutosPorLoja, getProdutoPorId, etc.) ...
    suspend fun listarTodos(): List<Produto> {
        return try {
            produtosCollection.get().await().toObjects(Produto::class.java)
        } catch (e: Exception) { emptyList() }
    }
    suspend fun getProdutosPorLoja(lojaId: String): List<Produto> {
        return try {
            produtosCollection.whereEqualTo("lojaId", lojaId).get().await().toObjects(Produto::class.java)
        } catch (e: Exception) { emptyList() }
    }
    suspend fun getProdutoPorId(produtoId: String): Produto? {
        return try {
            produtosCollection.document(produtoId).get().await().toObject(Produto::class.java)
        } catch (e: Exception) { null }
    }

    /**
     * NOVA FUNÇÃO: Deleta todos os produtos associados a uma loja.
     * Isso inclui deletar suas mídias do Cloud Storage.
     */
    suspend fun deletarProdutosPorLoja(lojaId: String) {
        val produtosParaDeletar = getProdutosPorLoja(lojaId)

        // Usa coroutines para deletar todos os produtos e suas mídias em paralelo
        coroutineScope {
            produtosParaDeletar.map { produto ->
                async(Dispatchers.IO) {
                    deletar(produto)
                }
            }.awaitAll()
        }
    }

    suspend fun deletar(produto: Produto) {
        produtosCollection.document(produto.id).delete().await()
        if (produto.imagemUrl.isNotBlank()) {
            try { storage.getReferenceFromUrl(produto.imagemUrl).delete().await() } catch (e: Exception) { /* Ignora */ }
        }
        if (produto.videoUrl.isNotBlank()) {
            try { storage.getReferenceFromUrl(produto.videoUrl).delete().await() } catch (e: Exception) { /* Ignora */ }
        }
    }

    suspend fun atualizar(produtoAtualizado: Produto) {
        produtosCollection.document(produtoAtualizado.id).set(produtoAtualizado).await()
    }

    suspend fun salvar(produto: Produto): String {
        val documentReference = produtosCollection.add(produto).await()
        return documentReference.id
    }

    suspend fun uploadMedia(uri: Uri, path: String, onProgress: (Double) -> Unit): String {
        val storageRef = storage.reference.child("$path/${System.currentTimeMillis()}")
        val uploadTask = storageRef.putFile(uri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            onProgress(progress)
        }.await()
        return storageRef.downloadUrl.await().toString()
    }
}