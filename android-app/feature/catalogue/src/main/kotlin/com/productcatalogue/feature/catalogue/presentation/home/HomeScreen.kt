package com.productcatalogue.feature.catalogue.presentation.home

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productcatalogue.core.designsystem.component.EmptyContent
import com.productcatalogue.core.designsystem.component.ErrorContent
import com.productcatalogue.core.designsystem.component.ProductCard
import com.productcatalogue.core.designsystem.component.ProductCardPlaceholder
import com.productcatalogue.core.designsystem.component.SearchEntryBar
import com.productcatalogue.core.designsystem.component.SectionHeader
import com.productcatalogue.core.designsystem.component.shimmer
import com.productcatalogue.core.model.Category
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.domain.model.HomeCatalogue
import com.productcatalogue.feature.catalogue.presentation.common.categoryIcon

/** Stateful entry point: connects the ViewModel to the stateless [HomeContent]. */
@Composable
internal fun HomeScreen(
    onProductClick: (Product) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onSearchClick = onSearchClick,
        onRetry = viewModel::retry,
        onProductClick = onProductClick,
        onCategoryClick = onCategoryClick,
    )
}

/** Stateless, so it can be previewed and UI-tested without a ViewModel. */
@Composable
internal fun HomeContent(
    state: HomeUiState,
    onSearchClick: () -> Unit,
    onRetry: () -> Unit,
    onProductClick: (Product) -> Unit,
    onCategoryClick: (Category) -> Unit,
) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("home"),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") { Header() }
            item(key = "search") {
                SearchEntryBar(onClick = onSearchClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            when (val catalogue = state.catalogue) {
                LoadState.Loading -> item(key = "loading") { LoadingSkeleton() }
                is LoadState.Error -> item(key = "error") {
                    ErrorContent(catalogue.message, onRetry, Modifier.height(480.dp))
                }
                is LoadState.Content -> {
                    item(key = "featured") { FeaturedSection(catalogue.value, onProductClick) }
                    item(key = "categories") { CategorySection(catalogue.value.categories, onCategoryClick) }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Icon(
                Icons.Filled.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(10.dp).size(24.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Catalogue", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Find something you'll love",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Each section is one Column: several children inside a single lazy item would overlap.
@Composable
private fun FeaturedSection(catalogue: HomeCatalogue, onProductClick: (Product) -> Unit) {
    Column {
        SectionHeader(
            "Top rated",
            subtitle = "Hand-picked by our team",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (catalogue.featured.isEmpty()) {
            EmptyContent("No featured products right now", hint = "Browse a category below instead.")
        } else {
            val listState = rememberLazyListState()
            LazyRow(
                state = listState,
                // Snaps a card to the edge after a swipe, like a carousel.
                flingBehavior = rememberSnapFlingBehavior(listState),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("featured"),
            ) {
                items(catalogue.featured, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) },
                        modifier = Modifier.width(188.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(categories: List<Category>, onCategoryClick: (Category) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Shop by category", modifier = Modifier.padding(top = 8.dp))
        // Two tiles per row; a plain Column because this already sits inside the page's LazyColumn.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.chunked(2).forEach { row ->
                // IntrinsicSize.Min + fillMaxHeight: both tiles in a row match the taller one.
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { category ->
                        CategoryTile(category, { onCategoryClick(category) }, Modifier.weight(1f).fillMaxHeight())
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("category-${category.slug}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(Modifier.fillMaxHeight().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(categoryIcon(category.slug), contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(category.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** The shape of the page while it loads: avoids a blank screen and layout jumps. */
@Composable
private fun LoadingSkeleton() {
    Column(Modifier.testTag("loading")) {
        Box(
            Modifier.padding(16.dp).width(140.dp).height(24.dp).clip(MaterialTheme.shapes.small).shimmer(),
        )
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { ProductCardPlaceholder(Modifier.width(188.dp)) }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        Box(Modifier.weight(1f).height(60.dp).clip(MaterialTheme.shapes.large).shimmer())
                    }
                }
            }
        }
    }
}
