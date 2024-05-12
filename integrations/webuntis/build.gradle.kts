plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(projects.integrations.api)
    implementation("org.mnode.ical4j:ical4j:4.0.0-rc6")

    testImplementation(tests.junit.api)
    testImplementation(libraries.kotlinx.serialization)
    testImplementation(ktor.serialization.kotlinx.json)
    testImplementation(projects.integrations.api)
}

tasks.test {
    useJUnitPlatform()
}
