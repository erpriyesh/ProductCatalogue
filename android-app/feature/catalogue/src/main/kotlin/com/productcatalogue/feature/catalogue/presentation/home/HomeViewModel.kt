package com.productcatalogue.feature.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productcatalogue.core.ui.loadStateFlow
import com.productcatalogue.feature.catalogue.domain.usecase.GetHomeCatalogueUseCase
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
class HomeViewModel @Inject constructor(
    private val getHomeCatalogue: GetHomeCatalogueUseCase,
) : ViewModel() {

    /** Bumped by [retry]; flatMapLatest cancels any load still running and starts a new one. */
    private val refresh = MutableStateFlow(0)

    /**
     * `Lazily`: the data loads once when the screen first appears and is kept for the life of
     * this back stack entry, so coming back from a product does not reload or flash a skeleton.
     * Search lives on its own screen; Home only opens it.
     */
    val uiState: StateFlow<HomeUiState> = refresh
        .flatMapLatest { loadStateFlow { getHomeCatalogue() } }
        .map { HomeUiState(catalogue = it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState())

    fun retry() = refresh.update { it + 1 }
}
