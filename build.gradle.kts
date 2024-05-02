import io.github.simulatan.gradle.plugin.buildinfo.configuration.BuildInfoExtension
import io.github.simulatan.gradle.plugin.buildinfo.configuration.PropertiesOutputLocation
import org.eclipse.jgit.api.Git
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

val isDevelopment: Boolean = project.findProperty("me.snoty.development")?.toString().toBoolean()

repositories {
    mavenCentral()
}

sourceSets {
    val main = main.get()
    val dev = create("dev") {
        compileClasspath += main.output
        runtimeClasspath += main.output
    }
    test {
        compileClasspath += dev.output
        runtimeClasspath += dev.output
    }
    create("testIntegration") {
        compileClasspath += main.output + test.get().output + dev.output
        runtimeClasspath += main.output + test.get().output + dev.output
    }
}

val devImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    // configuration
    implementation(configuration.hoplite.core)
    implementation(configuration.hoplite.yaml)
    implementation(configuration.hoplite.hikaricp)
    implementation(configuration.hoplite.datetime)

    implementation(ktor.serialization.kotlinx.json)

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
    implementation(ktor.server.contentNegotiation)

    // ktor client (used for OAuth2)
    implementation(ktor.client.core)
    implementation(ktor.client.apache)
    implementation(ktor.client.contentNegotiation)

    // monitoring
    implementation(monitoring.ktor.opentelemetry)
    implementation(monitoring.ktor.server.metricsMicrometer)
    implementation(monitoring.micrometer.prometheus)

    // database
    implementation(database.exposed.core)
    implementation(database.exposed.jdbc)
    implementation(database.exposed.json)
    implementation(database.postgres.driver)
    implementation(database.hikaricp)

    // logging
    implementation(log.logback)

    // serialization
    implementation(libraries.jackson.core)
    implementation(libraries.jackson.databind)
    implementation(libraries.jackson.kotlin)

    // task scheduling
    implementation(libraries.jobrunr)

    // testing
    testImplementation(tests.junit.api)
    testRuntimeOnly(tests.junit.engine)
    testRuntimeOnly(tests.junit.launcher)
    testImplementation(tests.ktor.server.tests)
    testImplementation(tests.mockk)
    testImplementation(tests.assertj.core)
    testImplementation(tests.json)
    testImplementation(tests.h2)
    testImplementation(tests.testcontainers)
    testImplementation(tests.testcontainers.junit)
    testImplementation(tests.testcontainers.keycloak)

    // dev
    devImplementation(dev.keycloak.adminClient)
}

application {
    mainClass.set("me.snoty.backend.ApplicationKt")

    if (isDevelopment) {
        applicationDefaultJvmArgs += "-Dio.ktor.development=$isDevelopment"
        tasks.run.configure {
            classpath += sourceSets["dev"].output
        }
    }
}

tasks.test {
    jvmArgs("-Dio.ktor.development=true")
    useJUnitPlatform()
}

val integrationTest: SourceSet = sourceSets["testIntegration"]


configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTestTask = tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs tests against integrations (database, LMS, etc)"

    useJUnitPlatform()

    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath

    shouldRunAfter("test")
}

tasks.check {
    dependsOn(integrationTestTask)
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
        val allTags = project.properties["me.snoty.docker.tags"]?.toString()?.trim()?.split(" ")?.toSet()
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
        ports = listOf("8080")
        val (ghaRunId, ghaRunNumber) =
            project.properties["me.snoty.github.run"]?.toString()?.split(":") ?: listOf(null, null)
        labels = mapOf(
            "org.opencontainers.image.title" to "snoty-backend",
            "org.opencontainers.image.description" to "Backend for the snoty project",
            "org.opencontainers.image.url" to "https://github.com/SnotyMe/snoty-backend/pkgs/container/snoty-backend",
            *Git.open(project.rootDir).use { git ->
                val headRef = git.repository.resolve("HEAD").name
                arrayOf(
                    "org.opencontainers.image.revision" to headRef,
                    // source to https version of the git repository
                    "org.opencontainers.image.source" to git.repository.config.getString("remote", "origin", "url")
                        .replace(":", "/")
                        .replace("git@", "https://")
                        .replace(".git", "")
                        + "/tree/$headRef"
                )
            },
            *if (ghaRunId != null && ghaRunNumber != null) arrayOf(
                "com.github.actions.run.id" to ghaRunId,
                "com.github.actions.run.number" to ghaRunNumber
            ) else arrayOf()
        )
    }
}

// intellij setup
idea {
    module {
        // long import times but worth it as, without it, functions may not have proper documentation
        isDownloadJavadoc = true
        isDownloadSources = true
        sourceDirs.minusAssign(file("src/testIntegration"))
        testSources.from(file("src/testIntegration/kotlin"))
    }

    project {
        settings {
            runConfigurations {
                // this run configuration emulates the `run` task, but without gradle
                // this *should* give better hot swap and performance
                create("Application [dev]", Application::class.java).apply {
                    mainClass = "me.snoty.backend.ApplicationKt"
                    moduleName = "snoty-backend.dev"
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
