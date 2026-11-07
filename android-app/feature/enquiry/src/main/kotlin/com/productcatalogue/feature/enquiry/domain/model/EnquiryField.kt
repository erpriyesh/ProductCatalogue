package com.productcatalogue.feature.enquiry.domain.model

/**
 * The enquiry form's fields. [key] is the field's name in the API, so field errors reported by
 * the server can be shown on the right input.
 */
enum class EnquiryField(val key: String) {
    NAME("name"),
    EMAIL("email"),
    PHONE("phone"),
    MESSAGE("message"),
    ;

    companion object {
        fun fromKey(key: String): EnquiryField? = entries.firstOrNull { it.key == key }
    }
}
