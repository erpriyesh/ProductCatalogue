package com.productcatalogue.feature.enquiry.presentation

import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.EnquiryFieldError
import com.productcatalogue.feature.enquiry.domain.model.FieldProblem
import com.productcatalogue.feature.enquiry.domain.usecase.ValidateEnquiryUseCase

/** The words shown under a form field. The domain says what is wrong; this says it to a customer. */
internal fun FieldProblem.message(field: EnquiryField): String = when (this) {
    is FieldProblem.FromServer -> message
    is FieldProblem.Rule -> when (error) {
        EnquiryFieldError.REQUIRED -> when (field) {
            EnquiryField.NAME -> "Enter your name"
            EnquiryField.EMAIL -> "Enter your email address"
            else -> "This field is required"
        }
        EnquiryFieldError.INVALID_FORMAT -> when (field) {
            EnquiryField.EMAIL -> "Enter a valid email address, like name@example.com"
            EnquiryField.PHONE -> "Enter a valid phone number"
            else -> "Check this field"
        }
        EnquiryFieldError.TOO_LONG -> "Use ${maxLength(field)} characters or fewer"
    }
}

private fun maxLength(field: EnquiryField) = when (field) {
    EnquiryField.NAME -> ValidateEnquiryUseCase.NAME_MAX
    EnquiryField.EMAIL -> ValidateEnquiryUseCase.EMAIL_MAX
    EnquiryField.MESSAGE -> ValidateEnquiryUseCase.MESSAGE_MAX
    EnquiryField.PHONE -> 20
}
