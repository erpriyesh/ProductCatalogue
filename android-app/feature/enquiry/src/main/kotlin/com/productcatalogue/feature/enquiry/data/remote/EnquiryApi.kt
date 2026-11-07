package com.productcatalogue.feature.enquiry.data.remote

import com.productcatalogue.feature.enquiry.data.remote.dto.EnquiryReceiptDto
import com.productcatalogue.feature.enquiry.data.remote.dto.EnquiryRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/** The enquiry endpoint of our backend. */
internal interface EnquiryApi {
    @POST("api/enquiries")
    suspend fun submitEnquiry(@Body request: EnquiryRequestDto): EnquiryReceiptDto
}
