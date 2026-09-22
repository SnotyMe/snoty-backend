plugins {
	id("snoty.extension-conventions")
}

dependencies {
	compileOnly(projects.adapter.adapterApi)

	implementation(libs.libraries.openfeature.flagd)
}
