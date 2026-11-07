package com.productcatalogue.feature.catalogue.domain.usecase

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.TestData
import com.productcatalogue.feature.catalogue.domain.model.HomeCatalogue
import com.productcatalogue.feature.catalogue.testing.FakeCatalogueRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetHomeCatalogueUseCaseTest {
    private val repository = FakeCatalogueRepository()
    private val getHomeCatalogue = GetHomeCatalogueUseCase(repository)

    @Test
    fun `combines featured products and categories`() = runTest {
        val featured = (1..5).map { TestData.product(it) }
        repository.featured = DataResult.Success(featured)

        assertEquals(
            DataResult.Success(HomeCatalogue(featured, listOf(TestData.menswear))),
            getHomeCatalogue(),
        )
    }

    @Test
    fun `fails when either call fails, so the page is never half loaded`() = runTest {
        repository.categories = DataResult.Failure(DataError.Timeout)
        assertEquals(DataResult.Failure(DataError.Timeout), getHomeCatalogue())

        repository.categories = DataResult.Success(emptyList())
        repository.featured = DataResult.Failure(DataError.NoConnection)
        assertEquals(DataResult.Failure(DataError.NoConnection), getHomeCatalogue())
    }
}
