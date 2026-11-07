package com.productcatalogue.feature.catalogue.presentation.search

import com.productcatalogue.feature.catalogue.domain.model.SearchQueryValidation
import com.productcatalogue.feature.catalogue.domain.usecase.ValidateSearchQueryUseCase

/**
 * The words shown under the search box. Null means the term is fine. "Too short" is shown as the
 * idle prompt on the search screen rather than as an error, since the customer is still typing.
 */
internal fun SearchQueryValidation.errorMessage(): String? = when (this) {
    is SearchQueryValidation.Valid -> null
    SearchQueryValidation.TooShort -> "Enter at least ${ValidateSearchQueryUseCase.MIN_LENGTH} characters"
    SearchQueryValidation.TooLong -> "Use ${ValidateSearchQueryUseCase.MAX_LENGTH} characters or fewer"
}
