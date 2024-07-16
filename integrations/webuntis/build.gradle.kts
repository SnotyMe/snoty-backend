plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
    alias(integrationPlugin.plugins.ksp)
}

dependencies {
    compileOnly(projects.integrations.api)
    implementation(projects.integrations.utils.calendar)

    testImplementation(tests.junit.api)
    testImplementation(libraries.kotlinx.serialization)
    testImplementation(ktor.serialization.kotlinx.json)
    testImplementation(projects.integrations.api)
    testImplementation(tests.mockk)

    ksp(projects.integrationPlugin)
}

sourceSets.test.configure {
    val integrationsApi = projects.integrations.api.dependencyProject.sourceSets.test.get().output
    compileClasspath += integrationsApi
    runtimeClasspath += integrationsApi
}

tasks.test {
    useJUnitPlatform()
}
