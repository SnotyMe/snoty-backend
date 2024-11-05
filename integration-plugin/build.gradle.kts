plugins {
	id("snoty.publish-conventions")
}

dependencies { with(libs) {
	implementation(integrationPlugin.ksp.api)
	implementation(projects.api)
	implementation("org.jetbrains.kotlin:kotlin-reflect:${libs.versions.kotlin.get()}")
	val kotlinpoet = "1.18.1"
	implementation("com.squareup:kotlinpoet-jvm:$kotlinpoet")
	implementation("com.squareup:kotlinpoet-ksp:$kotlinpoet")
	implementation("com.squareup:kotlinpoet-metadata:$kotlinpoet")
}}
