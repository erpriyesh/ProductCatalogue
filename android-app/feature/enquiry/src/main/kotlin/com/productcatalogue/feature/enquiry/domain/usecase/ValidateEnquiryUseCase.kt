package com.productcatalogue.feature.enquiry.domain.usecase

import com.productcatalogue.feature.enquiry.domain.model.EnquiryDraft
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.EnquiryFieldError
import javax.inject.Inject

/**
 * Mirrors the backend rules (backend/src/enquiry/enquiry.ts) for instant feedback. The backend
 * remains the authority; if the two ever disagree, the server field errors are shown instead.
 */
class ValidateEnquiryUseCase @Inject constructor() {

    operator fun invoke(draft: EnquiryDraft): Map<EnquiryField, EnquiryFieldError> = buildMap {
        val name = draft.name.trim()
        val email = draft.email.trim()
        val phone = draft.phone.trim()
        val message = draft.message.trim()

        when {
            name.isEmpty() -> put(EnquiryField.NAME, EnquiryFieldError.REQUIRED)
            name.length > NAME_MAX -> put(EnquiryField.NAME, EnquiryFieldError.TOO_LONG)
        }
        when {
            email.isEmpty() -> put(EnquiryField.EMAIL, EnquiryFieldError.REQUIRED)
            email.length > EMAIL_MAX -> put(EnquiryField.EMAIL, EnquiryFieldError.TOO_LONG)
            !EMAIL_PATTERN.matches(email) -> put(EnquiryField.EMAIL, EnquiryFieldError.INVALID_FORMAT)
        }
        if (phone.isNotEmpty() && !PHONE_PATTERN.matches(phone)) {
            put(EnquiryField.PHONE, EnquiryFieldError.INVALID_FORMAT)
        }
        if (message.length > MESSAGE_MAX) {
            put(EnquiryField.MESSAGE, EnquiryFieldError.TOO_LONG)
        }
    }

    companion object {
        const val NAME_MAX = 100
        const val EMAIL_MAX = 254
        const val MESSAGE_MAX = 1000

        // Deliberately simple: catches typos, the server does the strict check.
        private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        private val PHONE_PATTERN = Regex("^\\+?[0-9 ()-]{6,20}$")
    }
}
