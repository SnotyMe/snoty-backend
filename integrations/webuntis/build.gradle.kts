plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kover)
}

dependencies {
    compileOnly(projects.integrations.api)
    implementation(projects.integrations.utils.calendar)

    testImplementation(tests.junit.api)
    testImplementation(libraries.kotlinx.serialization)
    testImplementation(ktor.serialization.kotlinx.json)
    testImplementation(projects.integrations.api)
}

tasks.test {
    useJUnitPlatform()
}
