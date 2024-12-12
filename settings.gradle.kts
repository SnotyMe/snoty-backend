apply(from = "gradle/repositories.gradle.kts")

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "snoty-backend"

// include all integrations per default
File(rootDir, "integrations")
	.listFiles()!!
	.filter { it.resolve("build.gradle.kts").exists() }
	.forEach {
		include(":integrations:${it.name}")
	}

include("api")
include("integration-plugin")

includeBuild("integration-conventions")
includeBuild("publish-conventions")
