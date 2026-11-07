package com.productcatalogue.feature.catalogue.presentation.navigation

import kotlinx.serialization.Serializable

/** [query] pre-fills the search (e.g. from a deep link); empty opens a blank search. */
@Serializable
data class SearchRoute(val query: String = "")
