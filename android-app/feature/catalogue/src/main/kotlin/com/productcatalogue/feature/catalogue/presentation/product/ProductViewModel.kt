package com.productcatalogue.feature.catalogue.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.core.ui.loadStateFlow
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import com.productcatalogue.feature.catalogue.presentation.navigation.ProductRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogue: CatalogueRepository,
) : ViewModel() {

    private val productId = savedStateHandle.toRoute<ProductRoute>().productId
    private val refresh = MutableStateFlow(0)

    val uiState: StateFlow<LoadState<Product>> = refresh
        .flatMapLatest { loadStateFlow { catalogue.product(productId) } }
        .stateIn(viewModelScope, SharingStarted.Lazily, LoadState.Loading)

    fun retry() = refresh.update { it + 1 }
}
