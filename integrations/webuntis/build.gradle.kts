plugins {
    id("snoty.integration-conventions")
}

dependencies { with(libs) {
    testImplementation(libraries.kotlinx.serialization)
    testImplementation(ktor.serialization.kotlinx.json)
    testImplementation(testFixtures(projects.api))
}}
