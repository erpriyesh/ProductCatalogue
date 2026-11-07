package com.productcatalogue.feature.enquiry.data.di

import com.productcatalogue.feature.enquiry.data.remote.EnquiryApi
import com.productcatalogue.feature.enquiry.data.repository.NetworkEnquiryRepository
import com.productcatalogue.feature.enquiry.domain.repository.EnquiryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class EnquiryDataModule {

    @Binds
    @Singleton
    abstract fun bindEnquiryRepository(impl: NetworkEnquiryRepository): EnquiryRepository

    companion object {
        @Provides
        @Singleton
        fun provideEnquiryApi(retrofit: Retrofit): EnquiryApi = retrofit.create(EnquiryApi::class.java)
    }
}
