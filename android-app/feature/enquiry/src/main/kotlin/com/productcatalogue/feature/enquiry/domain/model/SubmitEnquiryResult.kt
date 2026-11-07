package com.productcatalogue.feature.enquiry.domain.model

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField

sealed interface SubmitEnquiryResult {
    /** The sales inbox accepted the email: show Thank you. */
    data class Sent(val reference: String) : SubmitEnquiryResult

    /** Some fields need fixing, found locally or by the server. Nothing was sent. */
    data class Rejected(val problems: Map<EnquiryField, FieldProblem>) : SubmitEnquiryResult

    /** The enquiry could not be sent (connection, email provider, rate limit). It can be retried. */
    data class Failed(val error: DataError) : SubmitEnquiryResult
}
