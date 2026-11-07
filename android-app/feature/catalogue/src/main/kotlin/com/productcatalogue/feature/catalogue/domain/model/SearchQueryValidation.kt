package com.productcatalogue.feature.catalogue.domain.model

/** The outcome of checking a search term. The UI turns the failure cases into words. */
sealed interface SearchQueryValidation {
    data class Valid(val term: String) : SearchQueryValidation
    data object TooShort : SearchQueryValidation
    data object TooLong : SearchQueryValidation
}
