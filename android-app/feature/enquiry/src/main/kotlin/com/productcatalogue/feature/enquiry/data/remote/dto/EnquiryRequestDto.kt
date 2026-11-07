package com.productcatalogue.feature.enquiry.data.remote.dto

import kotlinx.serialization.Serializable

/** Optional fields default to null and are left out of the JSON (see NetworkJson). */
@Serializable
data class EnquiryRequestDto(
    val productId: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String? = null,
)
