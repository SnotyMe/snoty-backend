enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "snoty-backend"

fun String.kebabCaseToCamelCase(): String {
	val pattern = "-[a-z]".toRegex()
	return replace(pattern) { it.value.last().uppercase() }
}

dependencyResolutionManagement {
	versionCatalogs {
		val ktorVersion = "3.0.0-beta-1"

		fun buildKtorArtifactAlias(prefix: String? = null, artifact: String, hierarchy: Boolean): String {
			var result: String = prefix ?: ""
			if (!prefix.isNullOrEmpty()) {
				result += "-"
			}

			result += if (hierarchy) {
				artifact
			} else {
				artifact.kebabCaseToCamelCase()
			}

			return result
		}

		fun VersionCatalogBuilder.ktorDependency(
			name: String,
			artifact: String = name,
			prefix: String? = null,
			hierarchy: Boolean = false
		) {
			library(
				buildKtorArtifactAlias(prefix, name, hierarchy),
				"io.ktor",
				"ktor-$artifact-jvm"
			).version(ktorVersion)
		}

		fun VersionCatalogBuilder.ktorPlugin(side: String, name: String, prefix: String? = null, hierarchy: Boolean = false)
			= ktorDependency(name, "$side-$name", (prefix?.let { "$it-" } ?: "") + side, hierarchy)

		fun VersionCatalogBuilder.ktorServerPlugin(name: String, prefix: String? = null, hierarchy: Boolean = false)
			= ktorPlugin("server", name, prefix, hierarchy)
		fun VersionCatalogBuilder.ktorClientPlugin(name: String, prefix: String? = null, hierarchy: Boolean = false)
			= ktorPlugin("client", name, prefix, hierarchy)

		val kotlinVersion = "1.9.23"

		fun VersionCatalogBuilder.kotlinPlugin(name: String) {
			plugin("kotlin-$name", "org.jetbrains.kotlin.plugin.$name")
				.version(kotlinVersion)
		}

		create("libs") {
			plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm")
				.version(kotlinVersion)
			kotlinPlugin("serialization")
			plugin("buildinfo", "io.github.simulatan.gradle-buildinfo-plugin")
				.version("2.1.0")
			plugin("jib", "com.google.cloud.tools.jib")
				.version("3.4.1")
			plugin("idea", "org.jetbrains.gradle.plugin.idea-ext")
				.version("1.1.8")
		}

		create("configuration") {
			val hoplite = version("hoplite", "2.8.0.RC3")

			fun hopliteDependency(name: String) {
				library("hoplite-$name", "com.sksamuel.hoplite", "hoplite-$name")
					.versionRef(hoplite)
			}

			hopliteDependency("core")
			hopliteDependency("yaml")
			hopliteDependency("hikaricp")
			hopliteDependency("datetime")
		}

		create("ktor") {
			ktorServerPlugin("core")
			ktorServerPlugin("netty")

			ktorServerPlugin("call-logging", hierarchy = true)
			ktorServerPlugin("call-id", hierarchy = true)
			ktorServerPlugin("swagger")
			ktorServerPlugin("openapi")
			ktorServerPlugin("forwarded-header")
			ktorServerPlugin("default-headers")
			ktorServerPlugin("host-common")
			ktorServerPlugin("status-pages")
			ktorServerPlugin("double-receive")
			ktorServerPlugin("auth")
			ktorServerPlugin("auth-jwt", hierarchy = true)
			ktorDependency("serialization-kotlinx-json", hierarchy = true)
			ktorServerPlugin("content-negotiation")

			ktorClientPlugin("core")
			ktorClientPlugin("apache")
			ktorClientPlugin("content-negotiation")
		}

		create("database") {
			val exposed = version("exposed", "0.49.0")

			fun exposedModule(name: String) {
				library("exposed-$name", "org.jetbrains.exposed", "exposed-$name")
					.versionRef(exposed)
			}

			exposedModule("core")
			exposedModule("jdbc")
			exposedModule("json")
			library("postgres-driver", "org.postgresql", "postgresql")
				.version("42.7.3")
			library("hikaricp", "com.zaxxer", "HikariCP")
				.version("5.1.0")
		}

		create("monitoring") {
			library("ktor-opentelemetry", "io.opentelemetry.instrumentation", "opentelemetry-ktor-2.0")
				.version("2.2.0-alpha")
			ktorServerPlugin("metrics-micrometer", prefix = "ktor")
			val micrometer = "1.6.3"
			library("micrometer-prometheus", "io.micrometer", "micrometer-registry-prometheus")
				.version(micrometer)
			library("micrometer", "io.micrometer", "micrometer-core")
				.version(micrometer)
		}

		create("log") {
			library("kotlinLogging", "io.github.oshai", "kotlin-logging")
				.version("6.0.9")
			library("logback", "ch.qos.logback", "logback-classic")
				.version("1.4.14")
		}

		create("libraries") {
			library("jobrunr", "org.jobrunr", "jobrunr").version("7.1.0")
			val jackson = version("jackson", "2.17.0")
			library("jackson-core", "com.fasterxml.jackson.core", "jackson-core").versionRef(jackson)
			library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef(jackson)
			library("jackson-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef(jackson)
			val kotlinxSerialization = version("kotlinx-serialization", "1.6.3")
			library("kotlinx-serialization", "org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef(kotlinxSerialization)
			library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef(kotlinxSerialization)
			library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.5.0")
		}

		create("tests") {
			ktorServerPlugin("tests", prefix = "ktor")
			library("mockk", "io.mockk", "mockk")
				.version("1.13.10")
			library("assertj-core", "org.assertj", "assertj-core")
				.version("3.25.3")
			library("json", "org.json", "json")
				.version("20240303")
			library("h2", "com.h2database", "h2")
				.version("2.2.224")

			val testcontainers = version("testcontainers", "1.19.7")
			library("testcontainers", "org.testcontainers", "testcontainers")
				.versionRef(testcontainers)
			library("testcontainers-junit", "org.testcontainers", "junit-jupiter")
				.versionRef(testcontainers)
			library("testcontainers-keycloak", "com.github.dasniko", "testcontainers-keycloak")
				.version("3.3.1")

			val junit = version("junit", "5.10.2")
			library("junit-api", "org.junit.jupiter", "junit-jupiter")
				.versionRef(junit)
			library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine")
				.versionRef(junit)
			// required to run tests with IntelliJ
			library("junit-launcher", "org.junit.platform", "junit-platform-launcher")
				.version("1.10.2")
		}

		create("dev") {
			library("keycloak-adminClient", "org.keycloak", "keycloak-admin-client")
				.version("24.0.2")
		}
	}
}

// include all integrations per default
File(rootDir, "integrations")
	.listFiles()!!
	.filter { it.resolve("build.gradle.kts").exists() }
	.forEach {
		include(":integrations:${it.name}")
	}
include("api")
include("integrations:moodle")
findProject(":integrations:moodle")?.name = "moodle"
include("integrations:webuntis")
findProject(":integrations:webuntis")?.name = "webuntis"
