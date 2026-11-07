package com.productcatalogue.feature.catalogue.testing

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.TestData
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import kotlinx.coroutines.CompletableDeferred

/**
 * A scriptable repository: each call returns what the test set, and calls are recorded.
 * A fake rather than a mock, so tests read like the real thing and survive refactors.
 */
class FakeCatalogueRepository : CatalogueRepository {
    var featured: DataResult<List<Product>> =
        DataResult.Success(listOf(TestData.product(1), TestData.product(2)))
    var categories: DataResult<List<Category>> = DataResult.Success(listOf(TestData.menswear))
    var categoryPage: DataResult<CategoryPage> = DataResult.Success(
        CategoryPage(TestData.menswear, listOf(TestData.product(1)), listOf(TestData.product(2))),
    )
    var searchResults: (String) -> DataResult<List<Product>> = { DataResult.Success(emptyList()) }
    var product: DataResult<Product> = DataResult.Success(TestData.product(1))

    /** When set, search() waits for it, so a test can start a second search while one is in flight. */
    var searchGate: CompletableDeferred<Unit>? = null

    val searches = mutableListOf<String>()
    val categoryRequests = mutableListOf<String>()
    val productRequests = mutableListOf<Int>()

    override suspend fun featuredProducts() = featured
    override suspend fun categories() = categories

    override suspend fun categoryPage(slug: String): DataResult<CategoryPage> {
        categoryRequests += slug
        return categoryPage
    }

    override suspend fun search(query: String): DataResult<List<Product>> {
        searches += query
        searchGate?.await()
        return searchResults(query)
    }

    override suspend fun product(id: Int): DataResult<Product> {
        productRequests += id
        return product
    }
}
