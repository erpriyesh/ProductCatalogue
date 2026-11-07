package com.productcatalogue.app.di

import com.productcatalogue.app.BuildConfig
import com.productcatalogue.core.network.ApiBaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Environment configuration comes from the app build (gradle.properties / -P flags), so library
 * modules stay environment-agnostic and a staging or production flavour only changes values here.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @ApiBaseUrl
    fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL
}
