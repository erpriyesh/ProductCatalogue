package com.productcatalogue.feature.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryListDto(val categories: List<CategoryDto>)
