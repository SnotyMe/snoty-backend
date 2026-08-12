plugins {
    id("snoty.integration-conventions")
    id("snoty.testintegration-conventions")
}

dependencies {
    api(libs.redis.lettuce)

    implementation(libs.libraries.coroutines.reactive)

    implementation(libs.cohort.api)
    implementation(libs.cohort.redis.lettuce)

    compileOnly(projects.adapter.adapterApi)
}
