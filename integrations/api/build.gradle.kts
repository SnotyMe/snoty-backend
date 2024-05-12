import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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

archivesName = "integration-api"

subprojects {
    archivesName = "integration-${this.name}"
}

tasks.test {
    useJUnitPlatform()
}
