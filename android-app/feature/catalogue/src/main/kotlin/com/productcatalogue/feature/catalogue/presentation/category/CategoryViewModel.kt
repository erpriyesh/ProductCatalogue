package com.productcatalogue.feature.catalogue.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.core.ui.loadStateFlow
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import com.productcatalogue.feature.catalogue.presentation.navigation.CategoryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogue: CatalogueRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CategoryRoute>()
    private val refresh = MutableStateFlow(0)

    val uiState: StateFlow<CategoryUiState> = refresh
        .flatMapLatest { loadStateFlow { catalogue.categoryPage(route.slug) } }
        .map { page ->
            CategoryUiState(
                // Prefer the server name once loaded, in case it changed since the link was shown.
                title = (page as? LoadState.Content)?.value?.category?.name ?: route.name,
                page = page,
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, CategoryUiState(title = route.name))

    fun retry() = refresh.update { it + 1 }
}
