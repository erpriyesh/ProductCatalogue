package com.productcatalogue.feature.catalogue.domain.repository

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage

/**
 * What the app needs from the catalogue. Defined in the domain layer and implemented in
 * :core:data, so features never know whether data comes from the network, a cache or a fake.
 *
 * One-shot reads are `suspend` functions: a request has one answer, not a stream. ViewModels
 * build their Flows on top. A Room-backed offline cache would add Flow-returning reads here.
 */
interface CatalogueRepository {
    suspend fun featuredProducts(): DataResult<List<Product>>
    suspend fun categories(): DataResult<List<Category>>
    suspend fun categoryPage(slug: String): DataResult<CategoryPage>
    suspend fun search(query: String): DataResult<List<Product>>
    suspend fun product(id: Int): DataResult<Product>
}
