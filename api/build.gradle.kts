plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(database.hikaricp)
    api(libraries.kotlinx.serialization)
    api(libraries.kotlinx.datetime)
    implementation(database.exposed.core)
    api(libraries.jobrunr)
    api(log.kotlinLogging)
}
