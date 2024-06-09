plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
}

dependencies {
    api(libraries.kotlinx.serialization)
    api(libraries.kotlinx.datetime)
    api(libraries.jobrunr)
    api(log.kotlinLogging)
    implementation(ktor.server.core)
    implementation(ktor.server.auth)
    implementation(ktor.server.auth.jwt)
    api(libraries.bson.kotlinx)
    api(database.mongodb)

    implementation(tests.junit.api)
}

tasks.test {
    useJUnitPlatform()
}
