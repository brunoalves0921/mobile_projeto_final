package com.example.ondetem.data

import android.content.Context
import android.net.Uri
import com.example.ondetem.ui.utils.compressImage
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

    // ================================================================
    // ===== NOVA FUNÇÃO PARA FAZER UPLOAD DE IMAGENS COMPRIMIDAS =====
    // ================================================================
    /**
     * Recebe uma lista de URIs, comprime cada uma usando ImageUtils e faz o upload.
     * @param context Contexto necessário para a função de compressão.
     * @param imageUris A lista de Uris das imagens a serem comprimidas e enviadas.
     * @return Uma lista com as URLs de download das imagens no Firebase Storage.
     */
    suspend fun uploadAndCompressImages(context: Context, imageUris: List<Uri>): List<String> {
        val imageUrls = mutableListOf<String>()
        coroutineScope {
            val uploadJobs = imageUris.map { uri ->
                async(Dispatchers.IO) {
                    // 1. Comprime a imagem primeiro
                    val compressedData = compressImage(context, uri)
                    if (compressedData != null) {
                        val storageRef = storage.reference.child("produtos/${UUID.randomUUID()}")
                        try {
                            // 2. Faz o upload dos bytes comprimidos
                            val uploadTask = storageRef.putBytes(compressedData).await()
                            uploadTask.storage.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    } else {
                        null
                    }
                }
            }
            imageUrls.addAll(uploadJobs.awaitAll().filterNotNull())
        }
        return imageUrls
    }


    /**
     * Faz o upload de um único arquivo de mídia (como um vídeo) sem compressão.
     * Mantemos esta função para o upload de vídeos.
     * @param mediaUri O Uri do arquivo.
     * @param path A pasta no Storage (ex: "videos").
     * @return A URL de download do arquivo.
     */
    suspend fun uploadRawFile(mediaUri: Uri, path: String): String? {
        return try {
            val storageRef = storage.reference.child("$path/${UUID.randomUUID()}")
            val uploadTask = storageRef.putFile(mediaUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    private suspend fun getProdutosPorLoja(lojaId: String): List<Produto> {
        return try {
            produtosCollection.whereEqualTo("lojaId", lojaId).get().await().toObjects(Produto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deletarProdutosPorLoja(lojaId: String) {
        val produtosParaDeletar = getProdutosPorLoja(lojaId)
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