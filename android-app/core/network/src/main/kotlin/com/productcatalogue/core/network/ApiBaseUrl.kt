package com.productcatalogue.core.network

import javax.inject.Qualifier

/**
 * The backend base URL (ending in a slash). Provided by the app module from its build config,
 * so this module has no idea which environment it talks to.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl
