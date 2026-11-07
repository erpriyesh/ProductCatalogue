package com.productcatalogue.feature.enquiry.domain.usecase

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.feature.enquiry.domain.model.Enquiry
import com.productcatalogue.feature.enquiry.domain.model.EnquiryDraft
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.FieldProblem
import com.productcatalogue.feature.enquiry.domain.model.SubmitEnquiryResult
import com.productcatalogue.feature.enquiry.domain.repository.EnquiryRepository
import javax.inject.Inject

/**
 * The enquiry critical path: validate, clean up, send, and turn every outcome into one of three
 * results the screen can show. Nothing is sent unless local validation passes.
 */
class SubmitEnquiryUseCase @Inject constructor(
    private val validate: ValidateEnquiryUseCase,
    private val enquiries: EnquiryRepository,
) {
    suspend operator fun invoke(productId: Int, draft: EnquiryDraft): SubmitEnquiryResult {
        val localErrors = validate(draft)
        if (localErrors.isNotEmpty()) {
            return SubmitEnquiryResult.Rejected(localErrors.mapValues { FieldProblem.Rule(it.value) })
        }

        val enquiry = Enquiry(
            productId = productId,
            name = draft.name.trim(),
            email = draft.email.trim(),
            phone = draft.phone.trim().ifEmpty { null },
            message = draft.message.trim().ifEmpty { null },
        )
        return when (val result = enquiries.submit(enquiry)) {
            is DataResult.Success -> SubmitEnquiryResult.Sent(result.value.reference)
            is DataResult.Failure -> result.error.toSubmitResult()
        }
    }

    private fun DataError.toSubmitResult(): SubmitEnquiryResult {
        val serverFieldErrors = (this as? DataError.Invalid)?.fieldErrors.orEmpty()
            .mapNotNull { (key, message) ->
                EnquiryField.fromKey(key)?.let { field -> field to FieldProblem.FromServer(message) }
            }
            .toMap()
        return if (serverFieldErrors.isNotEmpty()) {
            SubmitEnquiryResult.Rejected(serverFieldErrors)
        } else {
            SubmitEnquiryResult.Failed(this)
        }
    }
}
