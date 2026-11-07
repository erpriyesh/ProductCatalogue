package com.productcatalogue.core.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * ViewModel state is a lazily started StateFlow, so it only runs while collected, like on a real
 * screen. This subscribes for the rest of the test; `runTest` cancels it at the end.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.collectInBackground(flow: Flow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
}
