plugins {
	alias(libs.plugins.kotlin.dsl)
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	libs.plugins.kotlin.kover.get().apply {
		implementation("org.jetbrains.kotlinx:kover-gradle-plugin:$version")
	}
}
