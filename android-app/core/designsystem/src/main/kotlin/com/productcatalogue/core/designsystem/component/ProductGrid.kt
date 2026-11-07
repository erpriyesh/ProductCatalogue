package com.productcatalogue.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.productcatalogue.core.model.Product

/**
 * A grid that adapts its column count to the window width: two columns on a phone, more on a
 * tablet, foldable or in landscape. Items are keyed by product id so Compose reuses them.
 */
@Composable
fun ProductGrid(modifier: Modifier = Modifier, content: LazyGridScope.() -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** A grid row that spans every column, for section headers and empty states. */
fun LazyGridScope.fullWidthItem(key: Any? = null, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

/** Skeleton tiles shown while a grid of products loads. */
fun LazyGridScope.placeholderItems(count: Int = 6) {
    items(count) { ProductCardPlaceholder() }
}

fun LazyGridScope.productItems(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    badge: String? = null,
) {
    items(products, key = { it.id }) { product ->
        ProductCard(product = product, onClick = { onProductClick(product) }, badge = badge)
    }
}
