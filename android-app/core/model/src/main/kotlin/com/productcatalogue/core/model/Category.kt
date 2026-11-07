package com.productcatalogue.core.model

/** [slug] is the URL-safe id used in API calls; [name] is for display. */
data class Category(val slug: String, val name: String)
