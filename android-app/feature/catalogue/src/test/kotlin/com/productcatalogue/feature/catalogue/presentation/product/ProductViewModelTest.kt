package com.productcatalogue.feature.catalogue.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.MainDispatcherRule
import com.productcatalogue.core.testing.TestData
import com.productcatalogue.core.testing.collectInBackground
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.presentation.navigation.ProductRoute
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
class ProductViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val repository = FakeCatalogueRepository()
    private fun viewModel() = ProductViewModel(SavedStateHandle(route = ProductRoute(productId = 7)), repository)

    @Test
    fun `loads the product named in the route`() = runTest {
        repository.product = DataResult.Success(TestData.product(7))
        val viewModel = viewModel()
        collectInBackground(viewModel.uiState)

        assertEquals(listOf(7), repository.productRequests)
        assertEquals(LoadState.Content(TestData.product(7)), viewModel.uiState.value)
    }

    @Test
    fun `retry loads again after a failure`() = runTest {
        repository.product = DataResult.Failure(DataError.NoConnection)
        val viewModel = viewModel()
        collectInBackground(viewModel.uiState)

        repository.product = DataResult.Success(TestData.product(7))
        viewModel.retry()

        assertEquals(listOf(7, 7), repository.productRequests)
        assertEquals(LoadState.Content(TestData.product(7)), viewModel.uiState.value)
    }
}
