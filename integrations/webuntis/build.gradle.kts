plugins {
    id("snoty.integration-conventions")
}

dependencies { with(libs) {
    implementation(projects.integrations.utils.calendar)

    testImplementation(tests.junit.api)
    testImplementation(libraries.kotlinx.serialization)
    testImplementation(ktor.serialization.kotlinx.json)
    testImplementation(tests.mockk)
}}
