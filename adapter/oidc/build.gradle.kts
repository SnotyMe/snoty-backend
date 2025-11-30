plugins {
	id("snoty.integration-conventions")
	id("snoty.testintegration-conventions")
}

dependencies { with (libs) {
	api(ktor.client.core)

	compileOnly(projects.adapter.adapterApi)
}}
