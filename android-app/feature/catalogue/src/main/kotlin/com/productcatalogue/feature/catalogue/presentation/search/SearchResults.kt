package com.productcatalogue.feature.catalogue.presentation.search

import com.productcatalogue.core.model.Product

/** What the results area shows. */
sealed interface SearchResults {
    /** Nothing to search yet (empty or too short a term): show a prompt, not an error. */
    data object Idle : SearchResults

    data object Loading : SearchResults

    data class Error(val message: String) : SearchResults

    /** [products] are the matches for [term]; empty means "no results". */
    data class Found(val term: String, val products: List<Product>) : SearchResults
}
