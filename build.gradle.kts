import io.github.simulatan.gradle.plugin.buildinfo.configuration.BuildInfoExtension
import io.github.simulatan.gradle.plugin.buildinfo.configuration.PropertiesOutputLocation

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val hoplite_version: String by project

val prometeus_version: String by project
val exposed_version: String by project
val postgres_version: String by project
val hikari_version: String by project
val otl_version: String by project

val mockk_version: String by project
val assertj_version: String by project
val json_version: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("io.github.simulatan.gradle-buildinfo-plugin") version "2.1.0"
}

group = "me.snoty"
version = "0.0.1"

application {
    mainClass.set("me.snoty.backend.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.test {
    jvmArgs("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

dependencies {
    // dependency injection
    implementation(platform("io.insert-koin:koin-bom:$koin_version"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-ktor")
    implementation("io.insert-koin:koin-logger-slf4j")

    // configuration
    implementation("com.sksamuel.hoplite:hoplite-core:$hoplite_version")
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hoplite_version")
    implementation("com.sksamuel.hoplite:hoplite-hikaricp:$hoplite_version")
    implementation("com.sksamuel.hoplite:hoplite-datetime:$hoplite_version")

    // ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")

    // ktor plugins
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-swagger-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-forwarded-header-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-double-receive-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")

    // ktor client (OAuth2)
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache-jvm:$ktor_version")

    // monitoring
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-2.0:$otl_version")
    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-ktor-common:$otl_version")

    // database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")

    // logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.mockk:mockk:${mockk_version}")
    testImplementation("org.assertj:assertj-core:${assertj_version}")
    testImplementation("org.json:json:${json_version}")
    testImplementation("io.insert-koin:koin-test")
    testImplementation("io.insert-koin:koin-test-junit5")
}

buildInfo {
    val outputLocation = PropertiesOutputLocation { project ->
        listOf(project.layout.buildDirectory.get().file("info/buildinfo.properties").asFile)
    }
    propertiesOutputs = listOf(outputLocation)
    this.gitInfoMode = BuildInfoExtension.MODE_ERROR
    // ISO date
    committerDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    buildDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    extraAttribute("Version", version)
    extraAttribute("Application", rootProject.name)
}

tasks.classes.configure {
    dependsOn(tasks.buildInfo)
}

tasks.withType(JavaExec::class) {
    this.classpath += files("build/info")
}

tasks.jar {
    from("build/info")
}
