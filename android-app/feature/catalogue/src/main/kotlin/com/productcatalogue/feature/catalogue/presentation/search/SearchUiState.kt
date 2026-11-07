package com.productcatalogue.feature.catalogue.presentation.search

data class SearchUiState(
    /** What is in the text field, updated on every keystroke. */
    val query: String = "",
    /** Shown under the field, e.g. when the term is too long. Never shown for "too short" while typing. */
    val queryError: String? = null,
    val results: SearchResults = SearchResults.Idle,
)
