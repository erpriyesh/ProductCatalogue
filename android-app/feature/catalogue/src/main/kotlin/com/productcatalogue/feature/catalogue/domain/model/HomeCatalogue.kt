package com.productcatalogue.feature.catalogue.domain.model

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Product

/** What the home page shows: the products the business has chosen, and the category links. */
data class HomeCatalogue(
    val featured: List<Product>,
    val categories: List<Category>,
)
