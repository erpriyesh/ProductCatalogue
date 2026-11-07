package com.productcatalogue.feature.catalogue.domain.usecase

import com.productcatalogue.feature.catalogue.domain.model.SearchQueryValidation
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateSearchQueryUseCaseTest {
    private val validate = ValidateSearchQueryUseCase()

    @Test
    fun `trims and accepts 2 to 100 characters, like the backend`() {
        assertEquals(SearchQueryValidation.Valid("tv"), validate("  tv "))
        assertEquals(SearchQueryValidation.Valid("a".repeat(100)), validate("a".repeat(100)))
    }

    @Test
    fun `rejects terms that are too short or too long`() {
        assertEquals(SearchQueryValidation.TooShort, validate(" a "))
        assertEquals(SearchQueryValidation.TooShort, validate(""))
        assertEquals(SearchQueryValidation.TooLong, validate("a".repeat(101)))
    }
}
