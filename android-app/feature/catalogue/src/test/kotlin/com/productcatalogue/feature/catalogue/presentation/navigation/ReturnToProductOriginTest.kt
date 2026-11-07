package com.productcatalogue.feature.catalogue.presentation.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Stands in for the enquiry feature's screens, which sit on top of the product page. */
@Serializable
internal data object EnquiryFormStandIn

@Serializable
internal data object ThankYouStandIn

/**
 * After an enquiry, the customer goes back to where they opened the product (Home, a category or
 * search results), not to the product page. Uses a real back stack (TestNavHostController).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReturnToProductOriginTest {

    private val navController = TestNavHostController(RuntimeEnvironment.getApplication()).apply {
        navigatorProvider.addNavigator(ComposeNavigator())
        graph = createGraph(startDestination = HomeRoute) {
            composable<HomeRoute> {}
            composable<CategoryRoute> {}
            composable<SearchRoute> {}
            composable<ProductRoute> {}
            composable<EnquiryFormStandIn> {}
            composable<ThankYouStandIn> {}
        }
    }

    /** Product → Enquire → (form replaced by) Thank you, as the enquiry feature does it. */
    private fun openProductAndEnquire() {
        navController.navigate(ProductRoute(productId = 3))
        navController.navigate(EnquiryFormStandIn)
        navController.navigate(ThankYouStandIn) { popUpTo<EnquiryFormStandIn> { inclusive = true } }
    }

    private fun currentIs(check: (NavDestination) -> Boolean) =
        assertTrue(navController.currentDestination?.let(check) == true)

    @Test
    fun `product opened from Home returns to Home`() {
        openProductAndEnquire()
        navController.returnToProductOrigin()

        currentIs { it.hasRoute<HomeRoute>() }
        assertEquals(1, navController.currentBackStack.value.count { it.destination.route != null })
    }

    @Test
    fun `product opened from a category returns to that category`() {
        navController.navigate(CategoryRoute(slug = "mens-clothing", name = "Men's clothing"))
        openProductAndEnquire()
        navController.returnToProductOrigin()

        currentIs { it.hasRoute<CategoryRoute>() }
        assertEquals("mens-clothing", navController.currentBackStackEntry?.arguments?.getString("slug"))
    }

    @Test
    fun `product opened from search returns to the same search`() {
        navController.navigate(SearchRoute(query = "jacket"))
        openProductAndEnquire()
        navController.returnToProductOrigin()

        currentIs { it.hasRoute<SearchRoute>() }
        assertEquals("jacket", navController.currentBackStackEntry?.arguments?.getString("query"))
    }

    @Test
    fun `with no product page on the stack it falls back to Home`() {
        navController.navigate(ThankYouStandIn)
        navController.returnToProductOrigin()

        currentIs { it.hasRoute<HomeRoute>() }
    }
}
