plugins {
	`kotlin-dsl`
	alias(libs.plugins.kotlin.serialization)
	id("snoty.publish-repo-conventions")
}

apply(from = "../../version.gradle.kts")

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	libs.plugins.koin.compiler.get().apply {
		implementation("io.insert-koin.compiler.plugin:io.insert-koin.compiler.plugin.gradle.plugin:$version")
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
