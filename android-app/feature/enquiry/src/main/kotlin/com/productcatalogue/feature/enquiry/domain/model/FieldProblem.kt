package com.productcatalogue.feature.enquiry.domain.model

/** A problem with one form field: a local rule, or the message the server sent. */
sealed interface FieldProblem {
    data class Rule(val error: EnquiryFieldError) : FieldProblem
    data class FromServer(val message: String) : FieldProblem
}
