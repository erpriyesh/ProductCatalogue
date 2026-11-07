package com.productcatalogue.feature.catalogue.domain.model

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Product

/** A category's best-rated products, then everything else in it. The server decides the split. */
data class CategoryPage(
    val category: Category,
    val topRated: List<Product>,
    val others: List<Product>,
)
