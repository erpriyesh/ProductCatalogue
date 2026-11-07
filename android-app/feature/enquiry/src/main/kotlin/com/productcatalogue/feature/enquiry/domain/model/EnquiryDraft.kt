package com.productcatalogue.feature.enquiry.domain.model

/** What the customer has typed into the enquiry form, before validation. */
data class EnquiryDraft(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
)
