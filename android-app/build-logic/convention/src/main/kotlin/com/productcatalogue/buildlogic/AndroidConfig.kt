package com.productcatalogue.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** One place for the SDK levels and Java version every Android module uses. */
object AndroidConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 26
    const val TARGET_SDK = 37
    val JAVA_VERSION = JavaVersion.VERSION_17

    /** Robolectric's SDK 36 sandbox reaches into a JDK internal package on Java 21+. */
    const val ROBOLECTRIC_JVM_ARG = "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED"
}

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
