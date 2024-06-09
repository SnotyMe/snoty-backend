plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
}

dependencies {
    implementation(database.mongodb)
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
    api(libraries.bson.kotlinx)
    testImplementation(kotlin("test"))
    testImplementation(tests.mockk)
}

base.archivesName = "integration-api"

subprojects {
    base.archivesName = "integration-${this.name}"
}

tasks.test {
    useJUnitPlatform()
}
