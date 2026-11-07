package com.productcatalogue.core.ui

/** The state of anything a screen loads. Every screen renders all three cases. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Content<T>(val value: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}
