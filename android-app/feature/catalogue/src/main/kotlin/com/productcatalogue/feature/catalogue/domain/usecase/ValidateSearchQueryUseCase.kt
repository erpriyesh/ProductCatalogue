package com.productcatalogue.feature.catalogue.domain.usecase

import com.productcatalogue.feature.catalogue.domain.model.SearchQueryValidation
import javax.inject.Inject

/**
 * Same rule as the backend (2 to 100 characters after trimming) for instant feedback. The server
 * still checks: the app validation is for experience, the server validation is for safety.
 */
class ValidateSearchQueryUseCase @Inject constructor() {
    operator fun invoke(raw: String): SearchQueryValidation {
        val term = raw.trim()
        return when {
            term.length < MIN_LENGTH -> SearchQueryValidation.TooShort
            term.length > MAX_LENGTH -> SearchQueryValidation.TooLong
            else -> SearchQueryValidation.Valid(term)
        }
    }

    companion object {
        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 100
    }
}
