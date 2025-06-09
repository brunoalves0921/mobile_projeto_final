package com.example.ondetem.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object LojaRepository {
    private const val FILE_NAME = "lojas.json"

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    private fun getLojas(context: Context): List<Loja> {
        val file = getFile(context)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<Loja>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun listarTodas(context: Context): List<Loja> {
        return getLojas(context)
    }

    fun salvar(context: Context, loja: Loja) {
        val lojas = getLojas(context).toMutableList()
        lojas.add(loja)
        val json = Gson().toJson(lojas)
        getFile(context).writeText(json)
    }

    fun getLojasPorVendedor(context: Context, email: String): List<Loja> {
        // AQUI ESTÁ A CORREÇÃO:
        // Trocamos `it.donoEmail` por `it.donoId` para corresponder ao modelo de dados.
        // O parâmetro `email` agora, na verdade, contém o ID do vendedor (uid).
        return getLojas(context).filter { it.donoId == email }
    }
}