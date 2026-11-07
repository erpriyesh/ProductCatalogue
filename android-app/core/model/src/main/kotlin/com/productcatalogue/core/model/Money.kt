package com.productcatalogue.core.model

/**
 * Display-only money. The app never does arithmetic on prices (there is no cart), so a Double is
 * enough; a cart would switch to minor units or BigDecimal.
 */
data class Money(val amount: Double, val currencyCode: String)
