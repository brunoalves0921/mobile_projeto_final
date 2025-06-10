package com.example.ondetem.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation(
    private val locale: Locale = Locale("pt", "BR")
) : VisualTransformation {
    private val numberFormat = NumberFormat.getCurrencyInstance(locale)

    override fun filter(text: AnnotatedString): TransformedText {
        // Limpa o texto, mantendo apenas os dígitos
        val digitsOnly = text.text.filter { it.isDigit() }
        val amount = digitsOnly.toLongOrNull() ?: 0L

        // Formata o valor numérico como moeda
        val formattedAmount = numberFormat.format(amount / 100.0)

        // Mapeia os offsets para que o cursor se posicione corretamente
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return formattedAmount.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return digitsOnly.length
            }
        }

        return TransformedText(AnnotatedString(formattedAmount), offsetMapping)
    }
}

/**
 * Função de ajuda para formatar um valor em centavos (Long) para uma String de moeda.
 * Ex: 19925L -> "R$ 199,25"
 */
fun formatPrice(priceInCents: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(priceInCents / 100.0)
}