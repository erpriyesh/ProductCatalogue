package com.productcatalogue.feature.enquiry.domain.model

/** Why a field failed a local rule. The UI decides the wording. */
enum class EnquiryFieldError {
    REQUIRED,
    INVALID_FORMAT,
    TOO_LONG,
}
