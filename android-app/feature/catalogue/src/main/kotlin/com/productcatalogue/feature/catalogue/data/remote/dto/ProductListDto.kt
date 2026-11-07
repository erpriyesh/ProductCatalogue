package com.productcatalogue.feature.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

/** List responses are wrapped so fields such as a paging cursor can be added without breaking clients. */
@Serializable
data class ProductListDto(val products: List<ProductDto>)
