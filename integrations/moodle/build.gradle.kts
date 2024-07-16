plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
    alias(integrationPlugin.plugins.ksp)
}

dependencies {
    compileOnly(projects.integrations.api)
    implementation(projects.integrations.utils.calendar)

    ksp(projects.integrationPlugin)
}
