package com.productcatalogue.feature.catalogue.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Routes carry ids, not whole objects: each screen loads its own data, so it survives process
 * death and stays correct if the data changes.
 */
@Serializable
data class ProductRoute(val productId: Int)
