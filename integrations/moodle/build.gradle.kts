plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(projects.integrations.api)
    implementation(projects.integrations.utils.calendar)
}
