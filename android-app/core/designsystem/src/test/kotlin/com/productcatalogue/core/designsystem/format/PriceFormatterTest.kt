package com.productcatalogue.core.designsystem.format

import com.productcatalogue.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class PriceFormatterTest {
    @Test
    fun `formats in the currency the backend states, using the device locale`() {
        assertEquals("$1,234.50", formatPrice(Money(1234.5, "AUD"), Locale.forLanguageTag("en-AU")))
        // Same price on a US-locale device: the symbol shows it is Australian dollars.
        assertEquals("A$1,234.50", formatPrice(Money(1234.5, "AUD"), Locale.US))
    }

    @Test
    fun `an unknown currency code falls back to the code instead of crashing`() {
        assertEquals("XYZ1 9.90", formatPrice(Money(9.9, "XYZ1"), Locale.US))
    }

    @Test
    fun `whole amounts still show cents`() {
        assertTrue(formatPrice(Money(114.0, "AUD"), Locale.forLanguageTag("en-AU")).endsWith("114.00"))
    }
}
