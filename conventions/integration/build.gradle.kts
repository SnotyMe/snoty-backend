plugins {
	`kotlin-dsl`
	alias(libs.plugins.kotlin.serialization)
	id("snoty.publish-repo-conventions")
}

apply(from = "../../version.gradle.kts")

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	libs.integrationPlugin.ksp.api.get().apply {
		implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$version")
	}
}

publishing {
	publications {
		configureEach {
			if (this is MavenPublication) {
				artifactId = "conventions"
			}
		}
	}
}
