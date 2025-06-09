package com.example.ondetem.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ProdutoRepository {
    private const val FILE_NAME = "produtos_vendedor.json"

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    private fun getProdutos(context: Context): List<Produto> {
        val file = getFile(context)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<Produto>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    // --- FUNÇÕES NOVAS E ATUALIZADAS ---

    fun listarTodos(context: Context): List<Produto> {
        return getProdutos(context)
    }

    fun getProdutosPorLoja(context: Context, nomeLoja: String): List<Produto> {
        return getProdutos(context).filter { it.loja.equals(nomeLoja, ignoreCase = true) }
    }

    /**
     * NOVA FUNÇÃO: Busca um produto específico pelo seu ID.
     */
    fun getProdutoPorId(context: Context, produtoId: Int): Produto? {
        return getProdutos(context).firstOrNull { it.id == produtoId }
    }

    fun salvar(context: Context, produto: Produto) {
        val produtos = getProdutos(context).toMutableList()
        produtos.add(produto)
        val json = Gson().toJson(produtos)
        getFile(context).writeText(json)
    }

    /**
     * NOVA FUNÇÃO: Deleta um produto do arquivo JSON.
     * Também deleta os arquivos de imagem e vídeo associados.
     */
    fun deletar(context: Context, produtoId: Int) {
        val produtos = getProdutos(context).toMutableList()
        val produtoParaDeletar = produtos.firstOrNull { it.id == produtoId }

        if (produtoParaDeletar != null) {
            // Deleta o arquivo de imagem, se existir
            if (produtoParaDeletar.imagemUrl.isNotBlank()) {
                try { File(produtoParaDeletar.imagemUrl).delete() } catch (e: Exception) { /* Ignora erros */ }
            }
            // Deleta o arquivo de vídeo, se existir
            if (produtoParaDeletar.videoUrl.isNotBlank()) {
                try { File(produtoParaDeletar.videoUrl).delete() } catch (e: Exception) { /* Ignora erros */ }
            }
        }

        produtos.removeAll { it.id == produtoId }
        val json = Gson().toJson(produtos)
        getFile(context).writeText(json)
    }

    /**
     * NOVA FUNÇÃO: Atualiza um produto existente no arquivo JSON.
     */
    fun atualizar(context: Context, produtoAtualizado: Produto) {
        val produtos = getProdutos(context).toMutableList()
        val index = produtos.indexOfFirst { it.id == produtoAtualizado.id }
        if (index != -1) {
            produtos[index] = produtoAtualizado
            val json = Gson().toJson(produtos)
            getFile(context).writeText(json)
        }
    }
}