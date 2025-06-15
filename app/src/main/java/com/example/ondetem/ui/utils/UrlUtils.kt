package com.example.ondetem.ui.utils

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Constrói a URL de uma miniatura gerada pela extensão Resize Images do Firebase.
 * Esta é a versão final e corrigida que manipula o nome do arquivo corretamente.
 *
 * @param originalUrl A URL da imagem original salva no Firebase Storage.
 * @param size A dimensão desejada da miniatura (ex: "200x200").
 * @return A URL formatada para a miniatura, ou a URL original se ocorrer algum erro.
 */
fun getThumbnailUrl(originalUrl: String, size: String): String {
    if (originalUrl.isBlank() || !originalUrl.contains("firebasestorage.googleapis.com")) {
        return originalUrl
    }

    return try {
        // 1. Encontra a parte do caminho do arquivo, que começa depois de /o/
        val pathStartIndex = originalUrl.indexOf("/o/")
        if (pathStartIndex == -1) return originalUrl

        // 2. Separa a URL em duas partes: a base e o caminho do arquivo com o token
        val baseUrl = originalUrl.substring(0, pathStartIndex + 3) // Inclui o "/o/"
        val filePathAndToken = originalUrl.substring(pathStartIndex + 3)

        // 3. Encontra a separação entre o caminho do arquivo e o token
        val tokenStartIndex = filePathAndToken.indexOf("?alt=media")
        if (tokenStartIndex == -1) return originalUrl

        val encodedFilePath = filePathAndToken.substring(0, tokenStartIndex)
        val token = filePathAndToken.substring(tokenStartIndex)

        // 4. Decodifica o caminho para trabalhar com o nome do arquivo real
        // Ex: "produtos%2Fimagem.jpg" -> "produtos/imagem.jpg"
        val decodedFilePath = URLDecoder.decode(encodedFilePath, "UTF-8")

        // 5. Encontra a extensão e insere o sufixo
        val extensionIndex = decodedFilePath.lastIndexOf('.')
        if (extensionIndex == -1) return originalUrl

        val baseName = decodedFilePath.substring(0, extensionIndex)
        val extension = decodedFilePath.substring(extensionIndex)
        val newDecodedPath = "${baseName}_${size}${extension}"

        // 6. Codifica o novo caminho de volta para o formato de URL e remonta a URL final
        val newEncodedPath = URLEncoder.encode(newDecodedPath, "UTF-8")

        baseUrl + newEncodedPath + token
    } catch (e: Exception) {
        e.printStackTrace()
        originalUrl
    }
}