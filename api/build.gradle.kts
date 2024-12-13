plugins {
    alias(libs.plugins.kotlin.serialization)
    id("snoty.publish-conventions")
    id("snoty.publish-repo-conventions")
    `java-test-fixtures`
}

dependencies { with(libs) {
    api(configuration.hoplite.core)
    api(configuration.hoplite.datetime)

    api(libraries.kotlinx.serialization)
    api(libraries.kotlinx.datetime)

    api(libraries.jobrunr)

    api(log.kotlinLogging)

    api(ktor.client.core)
    api(ktor.client.apache)
    api(ktor.client.contentNegotiation)

    api(ktor.server.core)
    implementation(ktor.server.auth)
    implementation(ktor.server.auth.jwt)

    api(ktor.serialization.kotlinx.json)
    implementation(monitoring.ktor.opentelemetry)

    implementation(libraries.openfeature)

    api(database.mongodb)
    api(libraries.bson.kotlin)
    api(libraries.bson.kotlinx)

    implementation(monitoring.micrometer)
    api(monitoring.opentelemetry.api)
    api(monitoring.opentelemetry.context)

    testImplementation(tests.junit.api)
    testImplementation(tests.mockk)
    testFixturesImplementation(tests.mockk)
    testImplementation(kotlin("test"))
}}

tasks.test {
    useJUnitPlatform()
}
