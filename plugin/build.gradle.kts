plugins {
	id("snoty.testintegration-conventions")
	id("snoty.publish-conventions")
	id("snoty.publish-repo-conventions")
}

kotlin {
	compilerOptions {
		optIn.addAll(
			"com.squareup.kotlinpoet.DelicateKotlinPoetApi",
		)
	}
}

dependencies { with(libs) {
	implementation(projects.api)
	implementation(ksp.api)
	implementation("org.jetbrains.kotlin:kotlin-reflect:${libs.versions.kotlin.get()}")
	val kotlinpoet = "2.2.0"
	implementation("com.squareup:kotlinpoet-jvm:$kotlinpoet")
	implementation("com.squareup:kotlinpoet-ksp:$kotlinpoet")
	implementation("com.squareup:kotlinpoet-metadata:$kotlinpoet")
}}
