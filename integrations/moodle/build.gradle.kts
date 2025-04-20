plugins {
    id("snoty.integration-conventions")
}

dependencies {
    testImplementation(testFixtures(projects.api))
}
