package com.productcatalogue.core.network

import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Builds the networking stack. Plain functions so Hilt (NetworkModule) and tests share exactly
 * the same configuration.
 */
object NetworkFactory {

    /**
     * - ignoreUnknownKeys: the backend can add fields without breaking installed apps.
     * - explicitNulls = false: an empty optional field is left out of the JSON instead of being
     *   sent as null, which the backend strict schema would reject.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

    /**
     * @param cacheDir when set, responses are cached on disk. The backend sends
     *   `Cache-Control: public, max-age=60` for catalogue reads, so repeating a search or going
     *   back to a page within a minute is answered instantly without a network call.
     * @param logRequests logs method, URL, status and timing only (never bodies, which hold
     *   customer details). Debug builds only.
     */
    fun okHttpClient(cacheDir: File?, logRequests: Boolean): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .apply { if (cacheDir != null) cache(Cache(File(cacheDir, "http"), CACHE_SIZE_BYTES)) }
            .apply {
                if (logRequests) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

    fun retrofit(baseUrl: String, client: OkHttpClient, json: Json = this.json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
