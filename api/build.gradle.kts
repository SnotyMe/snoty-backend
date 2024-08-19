plugins {
    java
    alias(libs.plugins.kotlin.serialization)
}

dependencies { with(libs) {
    api(libraries.kotlinx.serialization)
    api(libraries.kotlinx.datetime)
    api(libraries.jobrunr)
    api(log.kotlinLogging)
    implementation(ktor.server.core)
    implementation(ktor.server.auth)
    implementation(ktor.server.auth.jwt)
    implementation(libraries.openfeature)
    api(libraries.bson.kotlinx)
    api(database.mongodb)
    api(monitoring.opentelemetry.api)
    api(monitoring.opentelemetry.context)

    implementation(tests.junit.api)
}}

tasks.test {
    useJUnitPlatform()
}
