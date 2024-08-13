@file:Suppress("UnstableApiUsage")

import io.github.simulatan.gradle.plugin.buildinfo.configuration.BuildInfoExtension
import io.github.simulatan.gradle.plugin.buildinfo.configuration.PropertiesOutputLocation
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    application
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildinfo)
    alias(libs.plugins.idea)
    id("snoty.kotlin-conventions")
    id("snoty.jib-conventions")
}

group = "me.snoty"
version = "0.0.1"

val isDevelopment: Boolean = project.findProperty("me.snoty.development")?.toString().toBoolean()

subprojects {
    apply(plugin = "snoty.kotlin-conventions")
}

val devSourceSet = sourceSets.create("dev") {
    val main = sourceSets.main.get()
    compileClasspath += main.output
    runtimeClasspath += main.output
}

lateinit var testIntegration: NamedDomainObjectProvider<JvmTestSuite>
testing {
    suites {
        val test by getting(JvmTestSuite::class)
        testIntegration = register<JvmTestSuite>("testIntegration") {
            dependencies {
                implementation(project())
                implementation(sourceSets.test.get().output)
            }
            sources.compileClasspath += sourceSets.test.get().compileClasspath
            sources.runtimeClasspath += sourceSets.test.get().runtimeClasspath
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
        withType<JvmTestSuite> {
            useJUnitJupiter()

            tasks.withType<Test>().configureEach {
                maxParallelForks = (Runtime.getRuntime().availableProcessors()).coerceAtLeast(1)
                environment("LOG_LEVEL", "TRACE")
            }

            dependencies {
                // API (contains things like Config)
                // for some reason, transitive dependencies aren't included in the test classpath
                implementation(projects.api)
                implementation(tests.junit.api)
                implementation(tests.ktor.server.testHost)
                implementation(tests.mockk)
                implementation(tests.assertj.core)
                implementation(tests.json)
                implementation(tests.h2)
                implementation(tests.testcontainers)
                implementation(tests.testcontainers.junit)
                implementation(tests.testcontainers.keycloak)
                implementation(tests.testcontainers.mongodb)
                implementation(monitoring.opentelemetry.testing)
                implementation(devSourceSet.output)

                runtimeOnly(tests.junit.engine)
                runtimeOnly(tests.junit.launcher)
            }
        }
    }
}

val devImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    fun moduleImplementation(dependency: Any) {
        implementation(dependency)
        testImplementation(dependency)
    }

    moduleImplementation(projects.api)

    // configuration
    implementation(configuration.hoplite.core)
    implementation(configuration.hoplite.yaml)
    implementation(configuration.hoplite.datetime)

    implementation(ktor.serialization.kotlinx.json)

    // ktor
    implementation(ktor.server.core)
    implementation(ktor.server.netty)

    // ktor plugins
    implementation(ktor.server.cors)
    implementation(ktor.server.call.logging)
    implementation(ktor.server.call.id)
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
    implementation(monitoring.opentelemetry.api)
    implementation(monitoring.opentelemetry.context)
    implementation(monitoring.opentelemetry.semconv)
    implementation(monitoring.opentelemetry.kotlin)
    implementation(monitoring.opentelemetry.logback)

    // database
    implementation(database.mongodb)
    implementation(database.mongodb.sync)
    implementation(libraries.bson.kotlinx)

    // logging
    implementation(log.logback)
    implementation(log.kotlinLogging)
    implementation(log.coroutines)

    // serialization
    implementation(libraries.jackson.core)
    implementation(libraries.jackson.databind)
    implementation(libraries.jackson.kotlin)

    // task scheduling
    implementation(libraries.jobrunr)

    // feature flags
    implementation(libraries.openfeature)
    implementation(libraries.openfeature.flagd)

    // dev
    devImplementation(dev.keycloak.adminClient)
    devImplementation(monitoring.opentelemetry.sdk)
    devImplementation(monitoring.opentelemetry.exporter.otlp)

    moduleImplementation(projects.integrations.api)
    // depend on all integrations by default
    subprojects
        .filter { it.path.startsWith(":integrations:") }
        .filter { !it.path.startsWith(":integrations:utils") }
        .forEach {
            moduleImplementation(it)
        }
}

if (isDevelopment) {
    tasks.run.configure {
        classpath += devSourceSet.output
    }
}

application {
    mainClass.set("me.snoty.backend.ApplicationKt")

    if (isDevelopment) {
        applicationDefaultJvmArgs += "-Dio.ktor.development=$isDevelopment"
    }
}

tasks.test {
    jvmArgs("-Dio.ktor.development=true")
}

tasks.check {
    dependsOn(testIntegration)
}

kover {
    currentProject {
        sources {
            excludedSourceSets.add(devSourceSet.name)
        }
    }
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
