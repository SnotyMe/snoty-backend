plugins {
	alias(libs.plugins.kotlin.jvm)
}

dependencies {
	compileOnly(projects.integrations.api)
}
