plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
}

dependencies {
    api(database.exposed.core)
    api(database.exposed.jdbc)
    api(database.exposed.json)
    api(monitoring.micrometer)
    api(ktor.client.core)
    api(ktor.client.apache)
    api(ktor.client.contentNegotiation)
    api(ktor.serialization.kotlinx.json)
    api(projects.api)
    api(ktor.server.core)
    api(ktor.server.auth)
    api(libraries.jackson.core)
    api(libraries.jackson.kotlin)
    testImplementation(kotlin("test"))
}

base.archivesName = "integration-api"

subprojects {
    base.archivesName = "integration-${this.name}"
}

tasks.test {
    useJUnitPlatform()
}
