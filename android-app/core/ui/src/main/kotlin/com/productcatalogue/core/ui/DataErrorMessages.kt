package com.productcatalogue.core.ui

import com.productcatalogue.core.model.result.DataError

/**
 * One place for the words a customer sees when something fails. Server messages for 5xx errors
 * are written for developers, so they are never shown; the error code picks the wording instead.
 */
fun DataError.userMessage(): String = when (this) {
    DataError.NoConnection -> "We can't reach the catalogue. Check your connection and try again."
    DataError.Timeout -> "The catalogue is taking too long to respond. Please try again."
    DataError.NotFound -> "We couldn't find that. It may no longer be available."
    DataError.RateLimited -> "You've sent a few of these already. Please wait a minute and try again."
    is DataError.Invalid -> message
    is DataError.Server -> when (code) {
        "EMAIL_DELIVERY_FAILED" -> "We couldn't send your enquiry just now. Please try again."
        else -> "Something went wrong on our side. Please try again."
    }
    DataError.Unexpected -> "Something went wrong. Please try again."
}
