plugins {
	alias(libs.plugins.kotlin.jvm)
}

dependencies {
	implementation(integrationPlugin.ksp.api)
	implementation(projects.integrations.api)
	val kotlinpoet = "1.18.1"
	implementation("com.squareup:kotlinpoet-jvm:$kotlinpoet")
	implementation("com.squareup:kotlinpoet-ksp:$kotlinpoet")
	implementation("com.squareup:kotlinpoet-metadata:$kotlinpoet")
}
