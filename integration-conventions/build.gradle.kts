plugins {
	alias(libs.plugins.kotlin.dsl)
	alias(libs.plugins.kotlin.serialization)
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	integrationPlugin.ksp.api.get().apply {
		implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$version")
	}
}
