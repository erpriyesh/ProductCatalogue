package com.productcatalogue.feature.catalogue.presentation.category

import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage

data class CategoryUiState(
    val title: String,
    val page: LoadState<CategoryPage> = LoadState.Loading,
)
