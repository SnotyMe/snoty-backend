plugins {
	alias(libs.plugins.kotlin.dsl)
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	libs.plugins.kotlin.kover.get().apply {
		implementation("org.jetbrains.kotlinx:kover-gradle-plugin:$version")
	}
	libs.plugins.jib.get().apply {
		implementation("com.google.cloud.tools:jib-gradle-plugin:$version")
	}
	implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
	libs.plugins.idea.get().apply {
		implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:$version")
	}
}
