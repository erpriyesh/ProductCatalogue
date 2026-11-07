package com.productcatalogue.feature.catalogue.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoneyDto(val amount: Double, val currency: String)
