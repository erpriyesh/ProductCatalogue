package com.productcatalogue.feature.catalogue.data.di

import com.productcatalogue.feature.catalogue.data.remote.CatalogueApi
import com.productcatalogue.feature.catalogue.data.repository.NetworkCatalogueRepository
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Wires this feature's data layer: its Retrofit service on the shared client, and its domain
 * repository interface to the network implementation. Going offline-first is a change here only.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class CatalogueDataModule {

    @Binds
    @Singleton
    abstract fun bindCatalogueRepository(impl: NetworkCatalogueRepository): CatalogueRepository

    companion object {
        @Provides
        @Singleton
        fun provideCatalogueApi(retrofit: Retrofit): CatalogueApi = retrofit.create(CatalogueApi::class.java)
    }
}
