apply(from = "gradle/repositories.gradle.kts")

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "snoty-backend"

fun listRootDirsIn(dir: String) =
	File(rootDir, dir)
		.listFiles()!!
		.filter { it.resolve("build.gradle.kts").exists() }

// include all integrations per default
listRootDirsIn("integrations")
	.forEach {
		include(":integrations:${it.name}")
	}

include("api")
include("integration-plugin")

listRootDirsIn("conventions")
	.forEach {
		includeBuild("conventions/${it.name}") {
			name = "conventions-${it.name}"
		}
	}
