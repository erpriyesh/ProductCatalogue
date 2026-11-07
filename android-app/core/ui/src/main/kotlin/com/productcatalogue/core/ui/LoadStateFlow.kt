package com.productcatalogue.core.ui

import com.productcatalogue.core.model.result.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Loading, then the result, as a cold Flow. ViewModels combine this with a refresh trigger and
 * `flatMapLatest`, so a retry (or a new search) cancels the request still in flight.
 */
fun <T> loadStateFlow(load: suspend () -> DataResult<T>): Flow<LoadState<T>> = flow {
    emit(LoadState.Loading)
    emit(load().toLoadState())
}

fun <T> DataResult<T>.toLoadState(): LoadState<T> = when (this) {
    is DataResult.Success -> LoadState.Content(value)
    is DataResult.Failure -> LoadState.Error(error.userMessage())
}
