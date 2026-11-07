package com.productcatalogue.feature.enquiry.data.repository

import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.network.safeApiCall
import com.productcatalogue.feature.enquiry.data.mapper.toRequest
import com.productcatalogue.feature.enquiry.data.remote.EnquiryApi
import com.productcatalogue.feature.enquiry.domain.model.Enquiry
import com.productcatalogue.feature.enquiry.domain.model.EnquiryReceipt
import com.productcatalogue.feature.enquiry.domain.repository.EnquiryRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

internal class NetworkEnquiryRepository @Inject constructor(
    private val api: EnquiryApi,
    private val json: Json,
) : EnquiryRepository {

    override suspend fun submit(enquiry: Enquiry): DataResult<EnquiryReceipt> =
        safeApiCall(json) { EnquiryReceipt(api.submitEnquiry(enquiry.toRequest()).reference) }
}
