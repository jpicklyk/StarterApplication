import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

class KnoxLicenseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            when {
                plugins.hasPlugin("com.android.application") -> {
                    extensions.configure<ApplicationExtension> {
                        configureBuildConfig(this)
                    }
                }
                plugins.hasPlugin("com.android.library") -> {
                    extensions.configure<LibraryExtension> {
                        configureBuildConfig(this)
                    }
                }
                else -> {
                    throw IllegalStateException("The 'convention.android.knox.license' plugin can only be applied to Android application or library projects.")
                }
            }
        }
    }

    private fun Project.configureBuildConfig(
        extension: CommonExtension<*, *, *, *, *, *>
    ) {
        extension.apply {
            buildFeatures {
                buildConfig = true
            }
            defaultConfig {
                buildConfigField(
                    type = "String",
                    name = "KNOX_LICENSE_KEY",
                    value = "\"${getKnoxLicenseKey()}\""
                )
            }
        }
    }

    private fun Project.getKnoxLicenseKey(): String {
        return getPropertyFromLocalProperties(
            key = "knox.license",
            defaultValue = "KNOX_LICENSE_KEY_NOT_FOUND"
        )
    }

    private fun Project.getPropertyFromLocalProperties(key: String, defaultValue: String = ""): String {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                localProperties.load(stream)
            }
        }
        return localProperties.getProperty(key, defaultValue)
    }
}