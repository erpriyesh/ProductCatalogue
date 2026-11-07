package com.productcatalogue.feature.catalogue.data.repository

import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.network.safeApiCall
import com.productcatalogue.feature.catalogue.data.mapper.toDomain
import com.productcatalogue.feature.catalogue.data.remote.CatalogueApi
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Catalogue data from our backend. Caching happens in OkHttp (HTTP cache headers), so this class
 * stays a thin, readable mapping layer. An offline-first version would add Room here.
 */
internal class NetworkCatalogueRepository @Inject constructor(
    private val api: CatalogueApi,
    private val json: Json,
) : CatalogueRepository {

    override suspend fun featuredProducts(): DataResult<List<Product>> =
        safeApiCall(json) { api.featuredProducts().products.map { it.toDomain() } }

    override suspend fun categories(): DataResult<List<Category>> =
        safeApiCall(json) { api.categories().categories.map { it.toDomain() } }

    override suspend fun categoryPage(slug: String): DataResult<CategoryPage> =
        safeApiCall(json) { api.categoryPage(slug).toDomain() }

    override suspend fun search(query: String): DataResult<List<Product>> =
        safeApiCall(json) { api.search(query).products.map { it.toDomain() } }

    override suspend fun product(id: Int): DataResult<Product> =
        safeApiCall(json) { api.product(id).toDomain() }
}
