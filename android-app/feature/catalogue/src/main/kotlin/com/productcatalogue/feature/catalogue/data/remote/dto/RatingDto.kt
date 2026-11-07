package com.productcatalogue.feature.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(val average: Double, val count: Int)
