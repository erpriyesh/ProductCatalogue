package com.productcatalogue.feature.catalogue.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productcatalogue.core.designsystem.component.BackTopBar
import com.productcatalogue.core.designsystem.component.EmptyContent
import com.productcatalogue.core.designsystem.component.ErrorContent
import com.productcatalogue.core.designsystem.component.ProductGrid
import com.productcatalogue.core.designsystem.component.SearchBar
import com.productcatalogue.core.designsystem.component.SectionHeader
import com.productcatalogue.core.designsystem.component.fullWidthItem
import com.productcatalogue.core.designsystem.component.placeholderItems
import com.productcatalogue.core.designsystem.component.productItems
import com.productcatalogue.core.model.Product

@Composable
internal fun SearchScreen(
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchContent(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onSearchNow = viewModel::onSearchNow,
        onRetry = viewModel::retry,
        onProductClick = onProductClick,
    )
}

@Composable
internal fun SearchContent(
    state: SearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchNow: () -> Unit,
    onRetry: () -> Unit,
    onProductClick: (Product) -> Unit,
) {
    // Opened from the home search bar: put the cursor in the field with the keyboard up.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (state.query.isEmpty()) focusRequester.requestFocus() }

    Scaffold(topBar = { BackTopBar("Search", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchBar(
                query = state.query,
                onQueryChange = onQueryChange,
                onSearchNow = onSearchNow,
                error = state.queryError,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).focusRequester(focusRequester),
            )
            when (val results = state.results) {
                SearchResults.Idle -> EmptyContent(
                    "Search the catalogue",
                    hint = "Type at least 2 letters, like “jacket” or “drive”.",
                    icon = Icons.AutoMirrored.Filled.ManageSearch,
                    modifier = Modifier.testTag("search-idle"),
                )
                SearchResults.Loading -> ProductGrid(Modifier.testTag("loading")) { placeholderItems() }
                is SearchResults.Error -> ErrorContent(results.message, onRetry)
                is SearchResults.Found -> ProductGrid(Modifier.testTag("search-results")) {
                    if (results.products.isEmpty()) {
                        fullWidthItem(key = "empty") {
                            EmptyContent(
                                "No products match “${results.term}”",
                                hint = "Check the spelling or try a broader word.",
                            )
                        }
                    } else {
                        fullWidthItem(key = "header") {
                            SectionHeader(
                                title = resultsTitle(results.products.size),
                                subtitle = "for “${results.term}”, best rated first",
                            )
                        }
                        productItems(results.products, onProductClick)
                    }
                }
            }
        }
    }
}

private fun resultsTitle(count: Int) = if (count == 1) "1 result" else "$count results"
