package com.productcatalogue.feature.enquiry.presentation.navigation

import kotlinx.serialization.Serializable

/** [productTitle] is for display only; the backend looks the product up by [productId]. */
@Serializable
data class EnquiryRoute(val productId: Int, val productTitle: String)
