package com.productcatalogue.feature.catalogue.data.remote

import com.productcatalogue.feature.catalogue.data.remote.dto.CategoryListDto
import com.productcatalogue.feature.catalogue.data.remote.dto.CategoryPageDto
import com.productcatalogue.feature.catalogue.data.remote.dto.ProductDto
import com.productcatalogue.feature.catalogue.data.remote.dto.ProductListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The catalogue endpoints of our backend. The app never calls Fake Store or any other product
 * source directly. A non-2xx response throws retrofit2.HttpException, which safeApiCall maps.
 */
internal interface CatalogueApi {
    @GET("api/products/featured")
    suspend fun featuredProducts(): ProductListDto

    @GET("api/categories")
    suspend fun categories(): CategoryListDto

    @GET("api/categories/{slug}/products")
    suspend fun categoryPage(@Path("slug") slug: String): CategoryPageDto

    @GET("api/products")
    suspend fun search(@Query("q") query: String): ProductListDto

    @GET("api/products/{id}")
    suspend fun product(@Path("id") id: Int): ProductDto
}
