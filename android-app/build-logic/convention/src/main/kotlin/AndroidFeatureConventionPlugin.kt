import com.productcatalogue.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Everything a feature module needs. A feature owns its whole vertical slice:
 *
 * - `data/`         Retrofit service, DTOs, mappers, repository implementation, Hilt module
 * - `domain/`       models, repository interface, use cases (no Android imports)
 * - `presentation/` screens, UI state, ViewModels, navigation graph
 *
 * It may use only the shared :core modules. Features never depend on other features; the app
 * module joins their navigation graphs.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("catalogue.android.library")
        pluginManager.apply("catalogue.android.compose")
        pluginManager.apply("catalogue.hilt")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:network"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:ui"))

            add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

            add("testImplementation", project(":core:testing"))
            add("testImplementation", libs.findLibrary("robolectric").get())
            add("testImplementation", libs.findLibrary("androidx-navigation-testing").get())
            add("testImplementation", libs.findLibrary("okhttp-mockwebserver").get())

            add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
            add("androidTestImplementation", libs.findLibrary("androidx-test-runner").get())
            // Compose UI test pulls an older Espresso that fails on API 36 (InputManager.getInstance).
            add("androidTestImplementation", libs.findLibrary("androidx-test-espresso-core").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
        }
    }
}
