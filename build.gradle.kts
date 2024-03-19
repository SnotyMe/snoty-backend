import io.github.simulatan.gradle.plugin.buildinfo.configuration.BuildInfoExtension
import io.github.simulatan.gradle.plugin.buildinfo.configuration.PropertiesOutputLocation
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

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
    id("com.google.cloud.tools.jib") version "3.4.1"

    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
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

tasks.buildInfo {
    // prevent circular dependency
    dependsOn.clear()
}

tasks.processResources {
    dependsOn(tasks.buildInfo)
    from("build/info")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        val allTags = project.properties["snoty.docker.tags"]?.toString()?.split(" ")?.toSet()
            ?: setOf(version.toString())
        image = "ghcr.io/snotyme/snoty-backend:${allTags.first()}"
        // workaround for the TERRIBLE design decisions of the JIB developers to
        // still generate the `latest` tag even when tags are specified...
        if (allTags.size > 1) {
            tags = allTags.drop(1).toSet()
        }
    }
    container {
        jvmFlags = listOf("-Dio.ktor.development=false")
        creationTime = "USE_CURRENT_TIMESTAMP"
        appRoot = "/app"
        workingDirectory = "/app"
    }
}

// intellij setup
idea {
    module {
        // long import times but worth it as, without it, functions may not have proper documentation
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    project {
        settings {
            runConfigurations {
                // this run configuration emulates the `run` task, but without gradle
                // this *should* give better hot swap and performance
                create("Application [dev]", Application::class.java).apply {
                    mainClass = "me.snoty.backend.ApplicationKt"
                    moduleName = "snoty-backend.main"
                    jvmArgs = "-Dio.ktor.development=true"

                    envs = mutableMapOf(
                        "LOG_LEVEL" to "TRACE",
                        "SERVER_LOG_LEVEL" to "INFO"
                    )
                }
            }
        }
    }
}
