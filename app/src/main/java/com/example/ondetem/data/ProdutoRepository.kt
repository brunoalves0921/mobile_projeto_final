package com.example.ondetem.data

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

object ProdutoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val produtosCollection = db.collection("produtos")

    /**
     * Busca todos os produtos do Firestore. Usado pela tela do cliente.
     */
    suspend fun listarTodos(): List<Produto> {
        return try {
            val snapshot = produtosCollection.get().await()
            snapshot.toObjects(Produto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Busca os produtos de uma loja específica pelo seu ID.
     */
    suspend fun getProdutosPorLoja(lojaId: String): List<Produto> {
        return try {
            val snapshot = produtosCollection.whereEqualTo("lojaId", lojaId).get().await()
            snapshot.toObjects(Produto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Busca um único produto pelo seu ID de documento.
     */
    suspend fun getProdutoPorId(produtoId: String): Produto? {
        return try {
            val snapshot = produtosCollection.document(produtoId).get().await()
            snapshot.toObject(Produto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deleta um produto do Firestore e suas mídias do Cloud Storage.
     */
    suspend fun deletar(produto: Produto) {
        // Deleta o documento do Firestore
        produtosCollection.document(produto.id).delete().await()

        // Deleta a imagem do Storage, se existir
        if (produto.imagemUrl.isNotBlank()) {
            try {
                storage.getReferenceFromUrl(produto.imagemUrl).delete().await()
            } catch (e: Exception) { /* Ignora erro se o arquivo não existir */
            }
        }
        // Deleta o vídeo do Storage, se existir
        if (produto.videoUrl.isNotBlank()) {
            try {
                storage.getReferenceFromUrl(produto.videoUrl).delete().await()
            } catch (e: Exception) { /* Ignora erro se o arquivo não existir */
            }
        }
    }

    /**
     * Atualiza um documento de produto no Firestore.
     */
    suspend fun atualizar(produtoAtualizado: Produto) {
        produtosCollection.document(produtoAtualizado.id).set(produtoAtualizado).await()
    }

    /**
     * Salva um novo produto no Firestore.
     */
    suspend fun salvar(produto: Produto) {
        produtosCollection.add(produto).await()
    }

    /**
     * Faz o upload de um arquivo (imagem ou vídeo) para o Cloud Storage.
     * @return A URL de download permanente do arquivo.
     */
    suspend fun uploadMedia(uri: Uri, path: String): String {
        val storageRef = storage.reference.child("$path/${System.currentTimeMillis()}")
        val uploadTask = storageRef.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }
}