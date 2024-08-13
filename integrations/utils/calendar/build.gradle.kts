plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    compileOnly(projects.integrations.api)
    api(libraries.ical4j)
}
