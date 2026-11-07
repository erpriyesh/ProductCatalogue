package com.productcatalogue.feature.enquiry.data.mapper

import com.productcatalogue.feature.enquiry.data.remote.dto.EnquiryRequestDto
import com.productcatalogue.feature.enquiry.domain.model.Enquiry

internal fun Enquiry.toRequest() = EnquiryRequestDto(
    productId = productId,
    name = name,
    email = email,
    phone = phone,
    message = message,
)
