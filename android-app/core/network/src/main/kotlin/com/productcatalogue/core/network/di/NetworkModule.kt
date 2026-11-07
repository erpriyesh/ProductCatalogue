package com.productcatalogue.core.network.di

import android.content.Context
import com.productcatalogue.core.network.ApiBaseUrl
import com.productcatalogue.core.network.BuildConfig
import com.productcatalogue.core.network.NetworkFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * The shared HTTP client. Each feature creates its own Retrofit service from this [Retrofit]
 * (see CatalogueDataModule, EnquiryDataModule), so features own their endpoints while sharing
 * one connection pool, cache, timeouts and JSON configuration.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = NetworkFactory.json

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        NetworkFactory.okHttpClient(cacheDir = context.cacheDir, logRequests = BuildConfig.DEBUG)

    @Provides
    @Singleton
    fun provideRetrofit(@ApiBaseUrl baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        NetworkFactory.retrofit(baseUrl, client, json)
}
