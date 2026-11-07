package com.productcatalogue.core.network.dto

import kotlinx.serialization.Serializable

/** One entry of a validation error details list. */
@Serializable
data class FieldIssueDto(val field: String, val message: String)
