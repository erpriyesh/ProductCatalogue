package com.productcatalogue.core.network.dto

import kotlinx.serialization.Serializable

/** The backend error envelope: { "error": { "code", "message", "details"? } }. */
@Serializable
data class ApiErrorDto(val error: ApiErrorBodyDto)
