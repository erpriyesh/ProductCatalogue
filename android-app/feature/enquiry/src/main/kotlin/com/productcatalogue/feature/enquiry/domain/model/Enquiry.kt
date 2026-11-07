package com.productcatalogue.feature.enquiry.domain.model

/** A validated enquiry, ready to send. Optional fields left blank are null. */
data class Enquiry(
    val productId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val message: String?,
)
