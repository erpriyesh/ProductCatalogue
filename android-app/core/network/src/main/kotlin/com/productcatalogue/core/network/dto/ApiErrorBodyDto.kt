package com.productcatalogue.core.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** [details] is a list of field issues for validation errors, absent otherwise. */
@Serializable
data class ApiErrorBodyDto(
    val code: String,
    val message: String,
    val details: JsonElement? = null,
)
