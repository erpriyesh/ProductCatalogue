package com.productcatalogue.core.model.result

/**
 * Every repository call returns a [DataResult] instead of throwing, so each caller must decide
 * what a failure means. The compiler checks that it does (`when` on a sealed type).
 */
sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}
