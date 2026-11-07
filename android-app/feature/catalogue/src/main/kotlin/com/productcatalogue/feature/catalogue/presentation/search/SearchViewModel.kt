package com.productcatalogue.feature.catalogue.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.core.ui.loadStateFlow
import com.productcatalogue.feature.catalogue.domain.model.SearchQueryValidation
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import com.productcatalogue.feature.catalogue.domain.usecase.ValidateSearchQueryUseCase
import com.productcatalogue.feature.catalogue.presentation.navigation.SearchRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Search-as-you-type as a Flow pipeline:
 *
 *   keystrokes ─ debounce(300 ms) ─┐
 *                                  ├─ merge ─ trim ─ distinctUntilChanged ─ (+ retry) ─ flatMapLatest ─ results
 *   keyboard Search key (now) ─────┘
 *
 * - `debounce` waits until typing pauses, so "jacket" is one request, not six.
 * - `distinctUntilChanged` skips a request when the term has not really changed ("jacket" vs "jacket ").
 * - `flatMapLatest` cancels a slower earlier search, so old results never overwrite newer ones.
 * - The keyboard Search key skips the wait (the "search button" of the acceptance criteria).
 * - Terms under 2 characters show a prompt rather than an error while the customer is still typing.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogue: CatalogueRepository,
    private val validateSearchQuery: ValidateSearchQueryUseCase,
) : ViewModel() {

    private val query = MutableStateFlow(savedStateHandle.toRoute<SearchRoute>().query)
    private val searchNow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val retry = MutableStateFlow(0)

    private val terms: Flow<String> = merge(query.debounce(DEBOUNCE_MS), searchNow)
        .map { it.trim() }
        .distinctUntilChanged()

    private val results: Flow<Pair<SearchResults, String?>> = combine(terms, retry) { term, _ -> term }
        .flatMapLatest { term -> searchFor(term) }

    val uiState: StateFlow<SearchUiState> = combine(query, results) { query, (results, error) ->
        SearchUiState(query = query, queryError = error, results = results)
    }.stateIn(viewModelScope, SharingStarted.Lazily, SearchUiState(query = query.value))

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Keyboard Search key: search immediately instead of waiting for the debounce. */
    fun onSearchNow() {
        searchNow.tryEmit(query.value)
    }

    fun retry() = retry.update { it + 1 }

    /** Results for one term, plus the message to show under the field (if any). */
    private fun searchFor(term: String): Flow<Pair<SearchResults, String?>> =
        when (val validation = validateSearchQuery(term)) {
            SearchQueryValidation.TooShort -> flowOf(SearchResults.Idle to null)
            SearchQueryValidation.TooLong -> flowOf(SearchResults.Idle to validation.errorMessage())
            is SearchQueryValidation.Valid -> loadStateFlow { catalogue.search(validation.term) }
                .map { state ->
                    when (state) {
                        LoadState.Loading -> SearchResults.Loading
                        is LoadState.Error -> SearchResults.Error(state.message)
                        is LoadState.Content -> SearchResults.Found(validation.term, state.value)
                    } to null
                }
        }

    companion object {
        /** Long enough to skip intermediate keystrokes, short enough to feel instant. */
        const val DEBOUNCE_MS = 300L
    }
}
