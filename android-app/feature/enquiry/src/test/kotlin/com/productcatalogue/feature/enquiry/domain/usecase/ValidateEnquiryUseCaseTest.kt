package com.productcatalogue.feature.enquiry.domain.usecase

import com.productcatalogue.feature.enquiry.domain.model.EnquiryDraft
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.EnquiryFieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateEnquiryUseCaseTest {
    private val validate = ValidateEnquiryUseCase()
    private val valid = EnquiryDraft(name = "Ada", email = "ada@example.com")

    @Test
    fun `name and email are enough`() {
        assertTrue(validate(valid).isEmpty())
    }

    @Test
    fun `name and email are required and whitespace does not count`() {
        assertEquals(
            mapOf(EnquiryField.NAME to EnquiryFieldError.REQUIRED, EnquiryField.EMAIL to EnquiryFieldError.REQUIRED),
            validate(EnquiryDraft(name = "   ", email = " ")),
        )
    }

    @Test
    fun `email must look like an address`() {
        listOf("ada", "ada@", "ada@example", "a da@example.com").forEach { email ->
            assertEquals(email, mapOf(EnquiryField.EMAIL to EnquiryFieldError.INVALID_FORMAT), validate(valid.copy(email = email)))
        }
    }

    @Test
    fun `phone is optional but must be a phone number when given`() {
        assertTrue(validate(valid.copy(phone = "+61 (02) 9000-0000")).isEmpty())
        assertEquals(mapOf(EnquiryField.PHONE to EnquiryFieldError.INVALID_FORMAT), validate(valid.copy(phone = "call me")))
    }

    @Test
    fun `length limits match the backend`() {
        val atLimit = valid.copy(name = "a".repeat(100), message = "m".repeat(1000))
        assertTrue(validate(atLimit).isEmpty())

        val overLimit = valid.copy(name = "a".repeat(101), message = "m".repeat(1001))
        assertEquals(
            mapOf(EnquiryField.NAME to EnquiryFieldError.TOO_LONG, EnquiryField.MESSAGE to EnquiryFieldError.TOO_LONG),
            validate(overLimit),
        )
    }
}
