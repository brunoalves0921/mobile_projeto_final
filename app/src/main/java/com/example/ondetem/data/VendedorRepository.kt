package com.example.ondetem.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VendedorRepository {
    private const val FILE_NAME = "vendedores.json"

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    fun salvar(context: Context, vendedor: Vendedor) {
        val vendedores = listar(context).toMutableList()
        vendedores.add(vendedor)
        val json = Gson().toJson(vendedores)
        getFile(context).writeText(json)
    }

    fun listar(context: Context): List<Vendedor> {
        val file = getFile(context)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<Vendedor>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun validarLogin(context: Context, email: String, senha: String): Boolean {
        return listar(context).any { it.email == email && it.senha == senha }
    }

    fun emailJaCadastrado(context: Context, email: String): Boolean {
        return listar(context).any { it.email == email }
    }
}
