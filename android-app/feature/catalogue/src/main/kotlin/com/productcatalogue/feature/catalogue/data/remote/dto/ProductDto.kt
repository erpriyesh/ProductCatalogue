package com.productcatalogue.feature.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

/** Wire format of a product (backend/src/catalogue/product.ts). */
@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: MoneyDto,
    val category: CategoryDto,
    val imageUrl: String,
    val rating: RatingDto,
)
