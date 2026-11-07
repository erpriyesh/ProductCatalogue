package com.productcatalogue.core.testing

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Money
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.model.Rating

/** Shared test data so every module builds products the same way. */
object TestData {
    val menswear = Category(slug = "mens-clothing", name = "Men's clothing")

    fun product(id: Int, title: String = "Product $id", rating: Double = 4.0) = Product(
        id = id,
        title = title,
        description = "Description of $title",
        price = Money(10.0 * id, "AUD"),
        category = menswear,
        imageUrl = "https://example.test/$id.png",
        rating = Rating(rating, 100),
    )
}
