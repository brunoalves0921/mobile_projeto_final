package com.example.ondetem.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Comprime uma imagem a partir de um Uri.
 *
 * @param context Contexto da aplicação.
 * @param imageUri O Uri da imagem a ser comprimida.
 * @param quality A qualidade da compressão JPEG (0-100). O padrão é 85.
 * @param maxResolution A resolução máxima (largura ou altura) da imagem. O padrão é 1280px.
 * @return Um ByteArray com os dados da imagem comprimida, ou nulo se ocorrer um erro.
 */
fun compressImage(
    context: Context,
    imageUri: Uri,
    quality: Int = 85,
    maxResolution: Int = 1280
): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val options = BitmapFactory.Options().apply {
            // Decodifica apenas os limites da imagem para obter as dimensões sem carregar na memória
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        val imgRatio = actualWidth.toFloat() / actualHeight.toFloat()

        // Redimensiona se a imagem for maior que a resolução máxima
        if (actualHeight > maxResolution || actualWidth > maxResolution) {
            if (actualHeight > actualWidth) {
                actualHeight = maxResolution
                actualWidth = (actualHeight * imgRatio).toInt()
            } else {
                actualWidth = maxResolution
                actualHeight = (actualWidth / imgRatio).toInt()
            }
        }

        // Calcula o 'inSampleSize' para carregar uma versão menor da imagem na memória, economizando recursos
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        options.inJustDecodeBounds = false

        val freshInputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(freshInputStream, null, options)
        freshInputStream?.close()

        val outputStream = ByteArrayOutputStream()
        // Comprime o bitmap para o formato JPEG com a qualidade especificada
        bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}