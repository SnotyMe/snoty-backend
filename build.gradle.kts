import io.github.simulatan.gradle.plugin.buildinfo.configuration.BuildInfoExtension
import io.github.simulatan.gradle.plugin.buildinfo.configuration.PropertiesOutputLocation
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
    alias(libs.plugins.buildinfo)
    alias(libs.plugins.jib)
    alias(libs.plugins.idea)
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
    implementation(configuration.hoplite.core)
    implementation(configuration.hoplite.yaml)
    implementation(configuration.hoplite.hikaricp)
    implementation(configuration.hoplite.datetime)

    // ktor
    implementation(ktor.server.core)
    implementation(ktor.server.netty)

    // ktor plugins
    implementation(ktor.server.call.logging)
    implementation(ktor.server.call.id)
    implementation(ktor.server.swagger)
    implementation(ktor.server.openapi)
    implementation(ktor.server.forwardedHeader)
    implementation(ktor.server.forwardedHeader)
    implementation(ktor.server.defaultHeaders)
    implementation(ktor.server.hostCommon)
    implementation(ktor.server.statusPages)
    implementation(ktor.server.doubleReceive)
    implementation(ktor.server.auth)
    implementation(ktor.server.auth.jwt)
    implementation(ktor.serialization.kotlinx.json)
    implementation(ktor.server.contentNegotiation)

    // ktor client (used for OAuth2)
    implementation(ktor.client.core)
    implementation(ktor.client.apache)

    // monitoring
    implementation(monitoring.ktor.opentelemetry)
    implementation(monitoring.ktor.server.metricsMicrometer)
    implementation(monitoring.micrometer.prometheus)

    // database
    implementation(database.exposed.core)
    implementation(database.exposed.jdbc)
    implementation(database.postgres.driver)
    implementation(database.hikaricp)

    // logging
    implementation(log.logback)

    // testing
    testImplementation(tests.ktor.server.tests)
    testImplementation(tests.kotlin.test.junit)
    testImplementation(tests.mockk)
    testImplementation(tests.assertj.core)
    testImplementation(tests.json)
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
