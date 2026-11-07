package com.productcatalogue.feature.enquiry.data.repository

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.network.NetworkFactory
import com.productcatalogue.feature.enquiry.data.remote.EnquiryApi
import com.productcatalogue.feature.enquiry.domain.model.Enquiry
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/** The enquiry data layer on the real network stack against a local MockWebServer. */
class NetworkEnquiryRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: NetworkEnquiryRepository

    @Before
    fun setUp() {
        server.start()
        val client = NetworkFactory.okHttpClient(cacheDir = null, logRequests = false)
            .newBuilder().readTimeout(2, TimeUnit.SECONDS).build()
        val api = NetworkFactory.retrofit(server.url("/").toString(), client).create(EnquiryApi::class.java)
        repository = NetworkEnquiryRepository(api, NetworkFactory.json)
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

    @Test
    fun `posts JSON and leaves out empty optional fields`() = runTest {
        respond(201, """{"reference":"ENQ-ABC"}""")

        val result = repository.submit(Enquiry(7, "Ada", "ada@example.com", phone = null, message = "Hi"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/enquiries", request.url.encodedPath)
        assertEquals(
            """{"productId":7,"name":"Ada","email":"ada@example.com","message":"Hi"}""",
            request.body?.utf8(),
        )
        assertEquals("ENQ-ABC", (result as DataResult.Success).value.reference)
    }

    @Test
    fun `validation errors are mapped per field`() = runTest {
        respond(
            400,
            """{"error":{"code":"VALIDATION_FAILED","message":"Some fields are invalid",
               "details":[{"field":"email","message":"Enter a valid email address"}]}}""",
        )

        val result = repository.submit(Enquiry(7, "Ada", "bad", null, null))

        val error = (result as DataResult.Failure).error as DataError.Invalid
        assertEquals(mapOf("email" to "Enter a valid email address"), error.fieldErrors)
    }

    @Test
    fun `an email provider failure keeps its error code`() = runTest {
        respond(502, """{"error":{"code":"EMAIL_DELIVERY_FAILED","message":"m"}}""")

        val result = repository.submit(Enquiry(7, "Ada", "ada@example.com", null, null))

        assertEquals(DataError.Server("EMAIL_DELIVERY_FAILED"), (result as DataResult.Failure).error)
    }
}
