package com.productcatalogue.feature.enquiry.domain.usecase

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.feature.enquiry.domain.model.Enquiry
import com.productcatalogue.feature.enquiry.domain.model.EnquiryDraft
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.EnquiryFieldError
import com.productcatalogue.feature.enquiry.domain.model.FieldProblem
import com.productcatalogue.feature.enquiry.domain.model.SubmitEnquiryResult
import com.productcatalogue.feature.enquiry.testing.FakeEnquiryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitEnquiryUseCaseTest {
    private val repository = FakeEnquiryRepository()
    private val submit = SubmitEnquiryUseCase(ValidateEnquiryUseCase(), repository)
    private val draft = EnquiryDraft(name = "  Ada Lovelace ", email = "ada@example.com ", message = "Is navy in stock?")

    @Test
    fun `sends a trimmed enquiry with blank optional fields left out`() = runTest {
        val result = submit(productId = 7, draft = draft)

        assertEquals(SubmitEnquiryResult.Sent("ENQ-1"), result)
        assertEquals(
            listOf(Enquiry(7, "Ada Lovelace", "ada@example.com", phone = null, message = "Is navy in stock?")),
            repository.submitted,
        )
    }

    @Test
    fun `invalid input is rejected without calling the server`() = runTest {
        val result = submit(productId = 7, draft = EnquiryDraft(email = "nope"))

        assertEquals(
            SubmitEnquiryResult.Rejected(
                mapOf(
                    EnquiryField.NAME to FieldProblem.Rule(EnquiryFieldError.REQUIRED),
                    EnquiryField.EMAIL to FieldProblem.Rule(EnquiryFieldError.INVALID_FORMAT),
                ),
            ),
            result,
        )
        assertTrue(repository.submitted.isEmpty())
    }

    @Test
    fun `server field errors are reported on the matching fields`() = runTest {
        repository.result = DataResult.Failure(
            DataError.Invalid("Some fields are invalid", mapOf("email" to "Enter a valid email address", "unknown" to "x")),
        )

        val result = submit(productId = 7, draft = draft)

        assertEquals(
            SubmitEnquiryResult.Rejected(mapOf(EnquiryField.EMAIL to FieldProblem.FromServer("Enter a valid email address"))),
            result,
        )
    }

    @Test
    fun `other failures are reported as retryable`() = runTest {
        repository.result = DataResult.Failure(DataError.Server("EMAIL_DELIVERY_FAILED"))
        assertEquals(SubmitEnquiryResult.Failed(DataError.Server("EMAIL_DELIVERY_FAILED")), submit(7, draft))
    }
}
