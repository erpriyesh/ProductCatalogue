package com.productcatalogue.feature.catalogue.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.productcatalogue.core.model.Product
import com.productcatalogue.feature.catalogue.presentation.category.CategoryScreen
import com.productcatalogue.feature.catalogue.presentation.home.HomeScreen
import com.productcatalogue.feature.catalogue.presentation.product.ProductScreen
import com.productcatalogue.feature.catalogue.presentation.search.SearchScreen

/**
 * The public API of this feature: its destinations and how to reach them. Anything that leaves
 * the feature (the Enquire button) is a callback, so the app module decides where it goes and
 * features never depend on each other.
 */
/**
 * Leaves the product page and everything opened from it (e.g. an enquiry), returning to the
 * screen the customer opened the product from: Home, a category or search results, with that
 * screen's state intact. Falls back to Home if no product page is on the back stack.
 */
fun NavController.returnToProductOrigin() {
    if (!popBackStack<ProductRoute>(inclusive = true)) {
        navigate(HomeRoute) { popUpTo<HomeRoute> { inclusive = true } }
    }
}

fun NavGraphBuilder.catalogueGraph(navController: NavController, onEnquire: (Product) -> Unit) {
    val back: () -> Unit = { navController.popBackStack() }
    val openProduct: (Product) -> Unit = { navController.navigate(ProductRoute(it.id)) }

    composable<HomeRoute> {
        HomeScreen(
            onProductClick = openProduct,
            onCategoryClick = { navController.navigate(CategoryRoute(it.slug, it.name)) },
            onSearchClick = { navController.navigate(SearchRoute()) },
        )
    }
    composable<CategoryRoute> {
        CategoryScreen(onBack = back, onProductClick = openProduct)
    }
    composable<SearchRoute> {
        SearchScreen(onBack = back, onProductClick = openProduct)
    }
    composable<ProductRoute> {
        ProductScreen(onBack = back, onEnquire = onEnquire)
    }
}
