package com.example.ondetem.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VendedorRepository {

    // AVISO: Este repositório agora tem uso limitado.
    // A autenticação (login/cadastro) é feita DIRETAMENTE pelo Firebase Authentication.
    // As informações do vendedor (nome, email) são salvas no FIRESTORE.
    //
    // Mantemos este arquivo para demonstrar a estrutura original, mas
    // as funções abaixo não são mais chamadas pelas telas de Login e Cadastro.

    private const val FILE_NAME = "vendedores.json"

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    // Esta função ainda pode ser útil para listar vendedores no futuro,
    // embora o ideal seja buscar do Firestore.
    fun listar(context: Context): List<Vendedor> {
        val file = getFile(context)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<Vendedor>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    // FUNÇÃO REMOVIDA DA LÓGICA DO APP
    // A validação de login agora é feita com `auth.signInWithEmailAndPassword(...)`
    fun validarLogin(context: Context, email: String, senha: String): Boolean {
        // A lógica antiga causava o erro. Como não é mais usada, retornamos `false`.
        return false
    }

    // FUNÇÃO REMOVIDA DA LÓGICA DO APP
    // A verificação de e-mail agora é tratada pelo Firebase.
    fun emailJaCadastrado(context: Context, email: String): Boolean {
        return listar(context).any { it.email == email }
    }
}