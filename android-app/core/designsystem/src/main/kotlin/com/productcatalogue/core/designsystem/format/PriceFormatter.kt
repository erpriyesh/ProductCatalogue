package com.productcatalogue.core.designsystem.format

import com.productcatalogue.core.model.Money
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formats a price in the currency the backend states, using the device locale for symbols and
 * separators: AUD 1234.5 is "$1,234.50" in en-AU and "A$1,234.50" in en-US. An unknown currency
 * code falls back to "CODE 1,234.50" rather than crashing.
 */
fun formatPrice(money: Money, locale: Locale = Locale.getDefault()): String {
    val currency = runCatching { Currency.getInstance(money.currencyCode) }.getOrNull()
        ?: return "${money.currencyCode} ${twoDecimals(locale).format(money.amount)}"
    return NumberFormat.getCurrencyInstance(locale)
        .apply { this.currency = currency }
        .format(money.amount)
}

private fun twoDecimals(locale: Locale) = NumberFormat.getNumberInstance(locale).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}
