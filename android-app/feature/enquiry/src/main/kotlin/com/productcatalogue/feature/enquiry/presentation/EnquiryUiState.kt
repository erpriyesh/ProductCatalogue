package com.productcatalogue.feature.enquiry.presentation

import com.productcatalogue.feature.enquiry.domain.model.EnquiryDraft
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.FieldProblem

data class EnquiryUiState(
    val productTitle: String,
    val draft: EnquiryDraft = EnquiryDraft(),
    val fieldProblems: Map<EnquiryField, FieldProblem> = emptyMap(),
    val isSubmitting: Boolean = false,
    /** A failure not tied to one field, e.g. no connection or the email could not be sent. */
    val submitError: String? = null,
    /** Set once the backend confirms the email was accepted; the screen then shows Thank you. */
    val sentReference: String? = null,
)
