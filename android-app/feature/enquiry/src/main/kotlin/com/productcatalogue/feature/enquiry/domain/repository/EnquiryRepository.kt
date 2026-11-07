package com.productcatalogue.feature.enquiry.domain.repository

import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.feature.enquiry.domain.model.Enquiry
import com.productcatalogue.feature.enquiry.domain.model.EnquiryReceipt

interface EnquiryRepository {
    /** Succeeds only once the backend has handed the email to the email provider. */
    suspend fun submit(enquiry: Enquiry): DataResult<EnquiryReceipt>
}
