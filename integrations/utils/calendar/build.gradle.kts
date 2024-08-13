plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(projects.integrations.api)
    api(libs.libraries.ical4j)
}
