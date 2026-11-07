package com.productcatalogue.feature.catalogue.data.repository

import com.productcatalogue.core.model.Money
import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.network.NetworkFactory
import com.productcatalogue.feature.catalogue.data.remote.CatalogueApi
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * This feature's data layer on the real Retrofit + OkHttp + kotlinx.serialization stack
 * (NetworkFactory) against a local MockWebServer: the requests we send, and how every kind of
 * response, including errors, is mapped.
 */
class NetworkCatalogueRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: NetworkCatalogueRepository

    @Before
    fun setUp() {
        server.start()
        val client = NetworkFactory.okHttpClient(cacheDir = null, logRequests = false)
            .newBuilder().readTimeout(2, TimeUnit.SECONDS).build()
        val api = NetworkFactory.retrofit(server.url("/").toString(), client).create(CatalogueApi::class.java)
        repository = NetworkCatalogueRepository(api, NetworkFactory.json)
    }

    @After
    fun tearDown() = server.close()

    private fun respond(code: Int, body: String) = server.enqueue(
        MockResponse.Builder()
            .code(code)
            .headers(headersOf("Content-Type", "application/json"))
            .body(body)
            .build(),
    )

    private val productJson = """
        {"id":3,"title":"Mens Cotton Jacket","description":"Warm","price":{"amount":55.99,"currency":"AUD"},
         "category":{"slug":"mens-clothing","name":"Men's clothing"},"imageUrl":"https://img.test/3.png",
         "rating":{"average":4.7,"count":500},"addedLater":"ignored"}
    """.trimIndent()

    @Test
    fun `maps featured products and tolerates fields it does not know`() = runTest {
        respond(200, """{"products":[$productJson]}""")

        val result = repository.featuredProducts()

        assertEquals("/api/products/featured", server.takeRequest().url.encodedPath)
        val product = (result as DataResult.Success).value.single()
        assertEquals(3, product.id)
        assertEquals(Money(55.99, "AUD"), product.price)
        assertEquals("Men's clothing", product.category.name)
    }

    @Test
    fun `search sends the term as an encoded query parameter`() = runTest {
        respond(200, """{"query":"mens jacket","products":[]}""")
        repository.search("mens jacket")
        assertEquals("mens jacket", server.takeRequest().url.queryParameter("q"))
    }

    @Test
    fun `status codes map to errors the UI understands`() = runTest {
        respond(404, """{"error":{"code":"NOT_FOUND","message":"m"}}""")
        respond(429, """{"error":{"code":"RATE_LIMITED","message":"m"}}""")
        respond(502, """{"error":{"code":"UPSTREAM_UNAVAILABLE","message":"m"}}""")
        respond(502, "<html>Bad gateway</html>")

        assertEquals(DataError.NotFound, (repository.product(1) as DataResult.Failure).error)
        assertEquals(DataError.RateLimited, (repository.product(1) as DataResult.Failure).error)
        assertEquals(DataError.Server("UPSTREAM_UNAVAILABLE"), (repository.product(1) as DataResult.Failure).error)
        // An error body that is not ours still maps by status.
        assertEquals(DataError.Server(null), (repository.product(1) as DataResult.Failure).error)
    }

    @Test
    fun `broken JSON and a dead server are reported, not thrown`() = runTest {
        respond(200, """{"categories":"oops"}""")
        assertTrue((repository.categories() as DataResult.Failure).error is DataError.Unexpected)

        server.close()
        assertEquals(DataError.NoConnection, (repository.categories() as DataResult.Failure).error)
    }
}
