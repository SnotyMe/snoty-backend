plugins {
	kotlin("plugin.serialization")
	id("snoty.integration-conventions")
	id("snoty.publish-repo-conventions")
}

dependencies {
	api(projects.api)
}
