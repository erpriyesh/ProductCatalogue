package com.productcatalogue.feature.catalogue.presentation.home

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.MainDispatcherRule
import com.productcatalogue.core.testing.TestData
import com.productcatalogue.core.testing.collectInBackground
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.domain.model.HomeCatalogue
import com.productcatalogue.feature.catalogue.domain.usecase.GetHomeCatalogueUseCase
import com.productcatalogue.feature.catalogue.testing.FakeCatalogueRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val repository = FakeCatalogueRepository()
    private fun viewModel() = HomeViewModel(GetHomeCatalogueUseCase(repository))

    @Test
    fun `shows the featured products and categories`() = runTest {
        val featured = (1..5).map { TestData.product(it) }
        repository.featured = DataResult.Success(featured)
        val viewModel = viewModel()
        collectInBackground(viewModel.uiState)

        assertEquals(
            LoadState.Content(HomeCatalogue(featured, listOf(TestData.menswear))),
            viewModel.uiState.value.catalogue,
        )
    }

    @Test
    fun `a failed load shows an error, and retry recovers`() = runTest {
        repository.featured = DataResult.Failure(DataError.NoConnection)
        val viewModel = viewModel()
        collectInBackground(viewModel.uiState)
        assertEquals(
            LoadState.Error("We can't reach the catalogue. Check your connection and try again."),
            viewModel.uiState.value.catalogue,
        )

        repository.featured = DataResult.Success(listOf(TestData.product(1)))
        viewModel.retry()
        assertTrue(viewModel.uiState.value.catalogue is LoadState.Content)
    }
}
