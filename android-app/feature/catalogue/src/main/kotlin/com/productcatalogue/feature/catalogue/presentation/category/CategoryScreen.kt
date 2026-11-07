package com.productcatalogue.feature.catalogue.presentation.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productcatalogue.core.designsystem.component.BackTopBar
import com.productcatalogue.core.designsystem.component.ErrorContent
import com.productcatalogue.core.designsystem.component.ProductGrid
import com.productcatalogue.core.designsystem.component.SectionHeader
import com.productcatalogue.core.designsystem.component.fullWidthItem
import com.productcatalogue.core.designsystem.component.placeholderItems
import com.productcatalogue.core.designsystem.component.productItems
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.domain.model.CategoryPage
import com.productcatalogue.feature.catalogue.presentation.common.categoryIcon

@Composable
internal fun CategoryScreen(
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CategoryContent(state, onBack, viewModel::retry, onProductClick)
}

@Composable
internal fun CategoryContent(
    state: CategoryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onProductClick: (Product) -> Unit,
) {
    Scaffold(topBar = { BackTopBar(state.title, onBack) }) { padding ->
        val modifier = Modifier.padding(padding)
        when (val page = state.page) {
            LoadState.Loading -> ProductGrid(modifier.testTag("loading")) { placeholderItems() }
            is LoadState.Error -> ErrorContent(page.message, onRetry, modifier)
            is LoadState.Content -> ProductGrid(modifier.testTag("category-products")) {
                fullWidthItem(key = "banner") { CategoryBanner(page.value) }
                fullWidthItem(key = "top-rated-header") {
                    // Ranked by a weighted rating on the server, so this subtitle is literally true.
                    SectionHeader("Top rated", subtitle = "Highly rated by the most customers")
                }
                productItems(page.value.topRated, onProductClick, badge = "Top rated")
                if (page.value.others.isNotEmpty()) {
                    fullWidthItem(key = "others-header") {
                        SectionHeader("More in ${page.value.category.name}")
                    }
                    productItems(page.value.others, onProductClick)
                }
            }
        }
    }
}

@Composable
private fun CategoryBanner(page: CategoryPage) {
    val count = page.topRated.size + page.others.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)) {
                Icon(
                    categoryIcon(page.category.slug),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(32.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(page.category.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    if (count == 1) "1 product" else "$count products",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
