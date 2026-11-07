package com.productcatalogue.feature.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryPageDto(
    val category: CategoryDto,
    val topRated: List<ProductDto>,
    val others: List<ProductDto>,
)
