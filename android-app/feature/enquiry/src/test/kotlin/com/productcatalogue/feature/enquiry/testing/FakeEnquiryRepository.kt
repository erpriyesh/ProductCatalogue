package com.productcatalogue.feature.enquiry.testing

import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.feature.enquiry.domain.model.Enquiry
import com.productcatalogue.feature.enquiry.domain.model.EnquiryReceipt
import com.productcatalogue.feature.enquiry.domain.repository.EnquiryRepository
import kotlinx.coroutines.CompletableDeferred

class FakeEnquiryRepository : EnquiryRepository {
    var result: DataResult<EnquiryReceipt> = DataResult.Success(EnquiryReceipt("ENQ-1"))

    /** When set, submit() waits for it, so a test can observe the in-flight state. */
    var gate: CompletableDeferred<Unit>? = null

    val submitted = mutableListOf<Enquiry>()

    override suspend fun submit(enquiry: Enquiry): DataResult<EnquiryReceipt> {
        submitted += enquiry
        gate?.await()
        return result
    }
}
