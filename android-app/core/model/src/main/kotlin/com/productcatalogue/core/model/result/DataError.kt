package com.productcatalogue.core.model.result

/** What went wrong, in terms the app can act on. The data layer builds these from HTTP results. */
sealed interface DataError {
    /** No network, DNS failure or connection refused (for example, the backend is not running). */
    data object NoConnection : DataError

    data object Timeout : DataError

    data object NotFound : DataError

    /** The server rejected the input. [fieldErrors] is keyed by the API field name. */
    data class Invalid(val message: String, val fieldErrors: Map<String, String>) : DataError

    data object RateLimited : DataError

    /**
     * The backend or something behind it failed. [code] is the backend error code. Its message
     * is meant for developers, so it is never shown to customers.
     */
    data class Server(val code: String?) : DataError

    /** An unexpected response shape or a bug. */
    data object Unexpected : DataError
}
