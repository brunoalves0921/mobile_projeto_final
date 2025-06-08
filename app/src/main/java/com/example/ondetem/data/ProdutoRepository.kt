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

    fun salvar(context: Context, produto: Produto) {
        val produtos = getProdutos(context).toMutableList()
        // Adiciona o novo produto. Poderíamos adicionar lógica para gerar ID único se necessário.
        produtos.add(produto)
        val json = Gson().toJson(produtos)
        getFile(context).writeText(json)
    }

    fun getProdutosPorLoja(context: Context, nomeLoja: String): List<Produto> {
        return getProdutos(context).filter { it.loja.equals(nomeLoja, ignoreCase = true) }
    }
}