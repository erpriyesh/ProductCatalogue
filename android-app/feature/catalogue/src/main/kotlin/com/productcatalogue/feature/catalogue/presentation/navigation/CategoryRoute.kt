package com.productcatalogue.feature.catalogue.presentation.navigation

import kotlinx.serialization.Serializable

/** [name] is passed only so the title shows before the page loads. */
@Serializable
data class CategoryRoute(val slug: String, val name: String)
