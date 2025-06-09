package com.example.ondetem.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object ProdutoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val produtosCollection = db.collection("produtos")

    suspend fun listarTodos(): List<Produto> {
        return try {
            produtosCollection.get().await().toObjects(Produto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProdutosPorLoja(lojaId: String): List<Produto> {
        return try {
            produtosCollection.whereEqualTo("lojaId", lojaId).get().await().toObjects(Produto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProdutoPorId(produtoId: String): Produto? {
        return try {
            produtosCollection.document(produtoId).get().await().toObject(Produto::class.java)
        } catch (e: Exception) {
            null
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

    /**
     * ATUALIZADO: Salva um novo produto e retorna o ID do documento criado.
     */
    suspend fun salvar(produto: Produto): String {
        val documentReference = produtosCollection.add(produto).await()
        return documentReference.id
    }

    /**
     * ATUALIZADO: Faz o upload da mídia e reporta o progresso.
     */
    suspend fun uploadMedia(uri: Uri, path: String, onProgress: (Double) -> Unit): String {
        val storageRef = storage.reference.child("$path/${System.currentTimeMillis()}")
        val uploadTask = storageRef.putFile(uri)

        // Adiciona um listener para o progresso do upload
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            onProgress(progress)
        }.await() // Espera a conclusão do upload

        return storageRef.downloadUrl.await().toString()
    }
}