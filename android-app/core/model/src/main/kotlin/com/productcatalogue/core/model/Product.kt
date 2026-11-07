package com.productcatalogue.core.model

/**
 * The app's product. Screens, ViewModels and use cases use this, never network DTOs, so a
 * change in the API's JSON is absorbed by the data layer's mappers.
 */
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Money,
    val category: Category,
    val imageUrl: String,
    val rating: Rating,
)
