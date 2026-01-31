@file:Suppress("UnstableApiUsage")


apply(from = "version.gradle.kts")

plugins {
    application
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    id("snoty.buildinfo-conventions")
    id("snoty.doctor-conventions")
    id("snoty.kotlin-conventions")
    id("snoty.idea-conventions")
    id("snoty.testintegration-conventions")
    id("snoty.publish-repo-conventions")
    id("snoty.koin-conventions")
}
// plugins applied after version.gradle.kts
apply(plugin = "snoty.catalog-conventions")
apply(plugin = "snoty.jib-conventions")

val isDevelopment: Boolean = project.findProperty("me.snoty.development")?.toString().toBoolean()

subprojects {
    apply(plugin = "snoty.kotlin-conventions")
    apply(from = "$rootDir/version.gradle.kts")
}

allprojects {
    apply(plugin = "snoty.koin-conventions")
}

val devSourceSet = sourceSets.create("dev") {
    val main = sourceSets.main.get()
    compileClasspath += main.output
    runtimeClasspath += main.output
}

testing.suites.withType<JvmTestSuite>().configureEach {
    dependencies { with(libs) {
        implementation(tests.ktor.server.testHost)
        implementation(tests.json)
        implementation(monitoring.opentelemetry.testing)
        implementation(devSourceSet.output)
    }}
}

val devImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies { with(libs) {
    fun moduleImplementation(dependency: Any) {
        implementation(dependency)
        testImplementation(dependency)
    }

    moduleImplementation(projects.api)
	moduleImplementation(projects.adapter.adapterApi)

	// database
    implementation(projects.adapter.mongodb)
    implementation(projects.adapter.sql)

	// authentication
	implementation(projects.adapter.oidc)
	implementation(projects.adapter.keycloak)

    implementation(koin.slf4j)
    implementation(libraries.coroutines.core)

    // configuration
    implementation(configuration.hoplite.yaml)

    // ktor server
    implementation(ktor.serialization.kotlinx.json)

    implementation(ktor.server.core)
    implementation(ktor.server.netty)

    // ktor plugins
    implementation(ktor.server.sse)
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
    implementation(ktor.server.routing.openapi)

    // monitoring
    implementation(monitoring.ktor.opentelemetry)
    implementation(monitoring.ktor.server.metricsMicrometer)
    implementation(monitoring.micrometer.prometheus)
    implementation(monitoring.opentelemetry.sdk.autoconfigure)
    implementation(monitoring.opentelemetry.exporter.otlp)
    implementation(monitoring.opentelemetry.kotlin)
    implementation(monitoring.opentelemetry.logback)

    // logging
    implementation(log.logback)
    implementation(log.kotlinLogging)
    implementation(log.coroutines)

    // feature flags
    implementation(libraries.openfeature)
    implementation(libraries.openfeature.flagd)

    // dev
    devImplementation(authentication.keycloak.adminClient)
    devImplementation(monitoring.opentelemetry.sdk)
    file("dist/integrations").listFiles()?.let {
        implementation(files(it))
    }

    // depend on all integrations by default
    subprojects
        .filter { it.path.startsWith(":integrations:") }
        .forEach {
            moduleImplementation(it)
        }
}}

if (isDevelopment) {
    tasks.run.configure {
        classpath += devSourceSet.output
    }
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}

application {
    mainClass.set("me.snoty.backend.MainKt")

    if (isDevelopment) {
        applicationDefaultJvmArgs += "-Dio.ktor.development=$isDevelopment"
    }
}

tasks.test {
    jvmArgs("-Dio.ktor.development=true")
}
