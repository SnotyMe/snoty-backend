apply(from = "gradle/repositories.gradle.kts")

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "snoty-backend"

fun listRootDirsIn(dir: String) =
	File(rootDir, dir)
		.listFiles()!!
		.filter { it.resolve("build.gradle.kts").exists() }

include("api")
include("plugin")
include("nodes")

listRootDirsIn("adapter")
	.forEach {
		include(":adapter:${it.name}")
	}

listRootDirsIn("conventions")
	.forEach {
		includeBuild("conventions/${it.name}") {
			name = "conventions-${it.name}"
		}
	}
