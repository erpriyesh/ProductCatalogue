package com.productcatalogue.feature.catalogue.domain.usecase

import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.feature.catalogue.domain.model.HomeCatalogue
import com.productcatalogue.feature.catalogue.domain.repository.CatalogueRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * The home page needs two independent calls. They run in parallel, and the page loads only if
 * both succeed: products without categories would look broken.
 */
class GetHomeCatalogueUseCase @Inject constructor(
    private val catalogue: CatalogueRepository,
) {
    suspend operator fun invoke(): DataResult<HomeCatalogue> = coroutineScope {
        val featured = async { catalogue.featuredProducts() }
        val categories = async { catalogue.categories() }
        when (val f = featured.await()) {
            is DataResult.Failure -> f
            is DataResult.Success -> when (val c = categories.await()) {
                is DataResult.Failure -> c
                is DataResult.Success -> DataResult.Success(HomeCatalogue(f.value, c.value))
            }
        }
    }
}
