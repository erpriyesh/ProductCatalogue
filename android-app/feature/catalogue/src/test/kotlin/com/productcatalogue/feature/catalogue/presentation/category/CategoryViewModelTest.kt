package com.productcatalogue.feature.catalogue.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.MainDispatcherRule
import com.productcatalogue.core.testing.TestData
import com.productcatalogue.core.testing.collectInBackground
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage
import com.productcatalogue.feature.catalogue.presentation.navigation.CategoryRoute
import com.productcatalogue.feature.catalogue.testing.FakeCatalogueRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CategoryViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val repository = FakeCatalogueRepository()
    private fun viewModel() =
        CategoryViewModel(SavedStateHandle(route = CategoryRoute("mens-clothing", "Mens")), repository)

    @Test
    fun `loads the category from the route and shows top rated and others`() = runTest {
        val page = CategoryPage(
            Category("mens-clothing", "Men's clothing"),
            topRated = listOf(TestData.product(2), TestData.product(4)),
            others = listOf(TestData.product(1)),
        )
        repository.categoryPage = DataResult.Success(page)
        val viewModel = viewModel()
        collectInBackground(viewModel.uiState)

        assertEquals(listOf("mens-clothing"), repository.categoryRequests)
        assertEquals(LoadState.Content(page), viewModel.uiState.value.page)
        // The server name replaces the one passed in the route.
        assertEquals("Men's clothing", viewModel.uiState.value.title)
    }

    @Test
    fun `a missing category shows a not found message and keeps the route title`() = runTest {
        repository.categoryPage = DataResult.Failure(DataError.NotFound)
        val viewModel = viewModel()
        collectInBackground(viewModel.uiState)

        assertEquals("Mens", viewModel.uiState.value.title)
        assertEquals(
            LoadState.Error("We couldn't find that. It may no longer be available."),
            viewModel.uiState.value.page,
        )
    }
}
