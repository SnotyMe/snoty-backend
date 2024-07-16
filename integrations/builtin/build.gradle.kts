plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.kover)
	alias(integrationPlugin.plugins.ksp)
}

dependencies {
	compileOnly(projects.integrations.api)
	ksp(projects.integrationPlugin)

	// liquid for java
	implementation("nl.big-o:liqp:0.9.0.3")
}
