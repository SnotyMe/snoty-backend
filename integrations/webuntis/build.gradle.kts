plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(projects.integrations.api)

    testImplementation(tests.junit.api)
    testImplementation(libraries.kotlinx.serialization)
    testImplementation(ktor.serialization.kotlinx.json)
    testImplementation(projects.integrations.api)
}
