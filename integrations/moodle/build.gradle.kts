plugins {
    id("snoty.integration-conventions")
}

dependencies {
    testImplementation(testFixtures(projects.api))
    testImplementation(libs.tests.mockk)
    testImplementation(libs.tests.junit.api)
}
