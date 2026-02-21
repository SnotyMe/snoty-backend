plugins {
	id("snoty.integration-conventions")
}

dependencies {
	compileOnly(projects.adapter.adapterApi)

	implementation(libs.libraries.openfeature.flagd)
}
