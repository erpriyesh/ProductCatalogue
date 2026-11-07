package com.productcatalogue.feature.catalogue.presentation.home

import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.domain.model.HomeCatalogue

/** Everything the home screen draws, in one immutable object. */
data class HomeUiState(
    val catalogue: LoadState<HomeCatalogue> = LoadState.Loading,
)
