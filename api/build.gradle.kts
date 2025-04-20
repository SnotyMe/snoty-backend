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
    api(log.logback)

    api(ktor.client.core)
    api(ktor.client.apache)
    api(ktor.client.contentNegotiation)

    api(ktor.server.core)
    api(ktor.server.auth)
    api(ktor.server.auth.jwt)

    api(ktor.serialization.kotlinx.json)
    api(monitoring.ktor.opentelemetry)

    api(libraries.openfeature)

    // DO NOT use in anything but bson-related code
    implementation(database.mongodb)
    api(libraries.bson.kotlin)
    api(libraries.bson.kotlinx)

    api(monitoring.micrometer)
    api(monitoring.opentelemetry.api)
    api(monitoring.opentelemetry.context)
    api(monitoring.opentelemetry.semconv)
    implementation(monitoring.opentelemetry.semconv.incubating)

    testFixturesImplementation(tests.json)
    testFixturesApi(tests.junit)
    testFixturesRuntimeOnly(tests.junit.launcher)
    testFixturesApi(kotlin("test"))
    testFixturesApi(koin.test)
    testFixturesApi(tests.mockk)
    testFixturesApi(tests.testcontainers.junit)
}}

tasks.test {
    useJUnitPlatform()
}
