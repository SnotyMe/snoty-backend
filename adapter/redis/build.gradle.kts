plugins {
    id("snoty.integration-conventions")
    id("snoty.testintegration-conventions")
}

dependencies {
    api(libs.redis.lettuce)

    implementation(libs.libraries.coroutines.reactive)

    compileOnly(projects.adapter.adapterApi)
}
