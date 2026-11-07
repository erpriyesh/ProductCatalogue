package com.productcatalogue.feature.catalogue.data.mapper

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Money
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.model.Rating
import com.productcatalogue.feature.catalogue.data.remote.dto.CategoryDto
import com.productcatalogue.feature.catalogue.data.remote.dto.CategoryPageDto
import com.productcatalogue.feature.catalogue.data.remote.dto.ProductDto
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage

/** DTO to domain mapping: the only code that knows both shapes. */

internal fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    price = Money(amount = price.amount, currencyCode = price.currency),
    category = category.toDomain(),
    imageUrl = imageUrl,
    rating = Rating(average = rating.average, count = rating.count),
)

internal fun CategoryDto.toDomain() = Category(slug = slug, name = name)

internal fun CategoryPageDto.toDomain() = CategoryPage(
    category = category.toDomain(),
    topRated = topRated.map { it.toDomain() },
    others = others.map { it.toDomain() },
)
