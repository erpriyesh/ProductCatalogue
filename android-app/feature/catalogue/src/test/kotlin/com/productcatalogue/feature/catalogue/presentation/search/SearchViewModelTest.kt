package com.productcatalogue.feature.catalogue.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.MainDispatcherRule
import com.productcatalogue.core.testing.TestData
import com.productcatalogue.core.testing.collectInBackground
import com.productcatalogue.feature.catalogue.domain.usecase.ValidateSearchQueryUseCase
import com.productcatalogue.feature.catalogue.presentation.navigation.SearchRoute
import com.productcatalogue.feature.catalogue.presentation.search.SearchViewModel.Companion.DEBOUNCE_MS
import com.productcatalogue.feature.catalogue.testing.FakeCatalogueRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Search-as-you-type on virtual time: `advanceTimeBy` moves the test clock, so the debounce can
 * be checked exactly without real waiting. Robolectric because the ViewModel reads its route.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val jacket = TestData.product(3, "Cotton Jacket")
    private val repository = FakeCatalogueRepository().apply {
        searchResults = { term -> DataResult.Success(if (term == "jacket") listOf(jacket) else emptyList()) }
    }

    private fun TestScope.viewModel(query: String = ""): SearchViewModel =
        SearchViewModel(SavedStateHandle(route = SearchRoute(query)), repository, ValidateSearchQueryUseCase())
            .also { collectInBackground(it.uiState) }

    private fun TestScope.waitForDebounce() {
        advanceTimeBy(DEBOUNCE_MS)
        runCurrent()
    }

    @Test
    fun `starts idle with nothing searched`() = runTest {
        val viewModel = viewModel()
        waitForDebounce()

        assertEquals(SearchResults.Idle, viewModel.uiState.value.results)
        assertTrue(repository.searches.isEmpty())
    }

    @Test
    fun `typing quickly makes one request, after the customer pauses`() = runTest {
        val viewModel = viewModel()
        listOf("j", "ja", "jac", "jack", "jacke", "jacket").forEach {
            viewModel.onQueryChange(it)
            advanceTimeBy(100)
        }
        advanceTimeBy(DEBOUNCE_MS - 101)
        runCurrent()
        assertTrue("no request before the pause", repository.searches.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("jacket"), repository.searches)
        assertEquals(SearchResults.Found("jacket", listOf(jacket)), viewModel.uiState.value.results)
    }

    @Test
    fun `a term that is too short shows the prompt, not an error, and searches nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("j")
        waitForDebounce()

        assertEquals(SearchResults.Idle, viewModel.uiState.value.results)
        assertNull(viewModel.uiState.value.queryError)
        assertTrue(repository.searches.isEmpty())
    }

    @Test
    fun `a term that is too long explains why`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("a".repeat(101))
        waitForDebounce()

        assertEquals("Use 100 characters or fewer", viewModel.uiState.value.queryError)
        assertTrue(repository.searches.isEmpty())
    }

    @Test
    fun `the keyboard search key searches at once, and the debounce does not repeat it`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("jacket")
        viewModel.onSearchNow()
        runCurrent()
        assertEquals(listOf("jacket"), repository.searches)

        waitForDebounce()
        assertEquals(listOf("jacket"), repository.searches)
    }

    @Test
    fun `whitespace changes do not search again`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("jacket")
        waitForDebounce()
        viewModel.onQueryChange("jacket ")
        waitForDebounce()

        assertEquals(listOf("jacket"), repository.searches)
    }

    @Test
    fun `opening with a query searches for it`() = runTest {
        val viewModel = viewModel("jacket")
        waitForDebounce()

        assertEquals(listOf("jacket"), repository.searches)
        assertEquals("jacket", viewModel.uiState.value.query)
    }

    @Test
    fun `a newer search cancels a slower one, so old results never overwrite new ones`() = runTest {
        val slowFirstSearch = CompletableDeferred<Unit>()
        repository.searchGate = slowFirstSearch
        val viewModel = viewModel()
        viewModel.onQueryChange("jacket")
        waitForDebounce()
        assertEquals(SearchResults.Loading, viewModel.uiState.value.results)

        // "jacket" is still in flight when the customer changes it to "shoes".
        repository.searchGate = null
        viewModel.onQueryChange("shoes")
        waitForDebounce()
        // Now the slow "jacket" response arrives. It must not replace the "shoes" results.
        slowFirstSearch.complete(Unit)
        runCurrent()

        assertEquals(SearchResults.Found("shoes", emptyList()), viewModel.uiState.value.results)
    }

    @Test
    fun `a failure can be retried for the same term`() = runTest {
        repository.searchResults = { DataResult.Failure(DataError.Timeout) }
        val viewModel = viewModel()
        viewModel.onQueryChange("jacket")
        waitForDebounce()
        assertTrue(viewModel.uiState.value.results is SearchResults.Error)

        repository.searchResults = { DataResult.Success(listOf(jacket)) }
        viewModel.retry()
        runCurrent()

        assertEquals(listOf("jacket", "jacket"), repository.searches)
        assertEquals(SearchResults.Found("jacket", listOf(jacket)), viewModel.uiState.value.results)
    }
}
