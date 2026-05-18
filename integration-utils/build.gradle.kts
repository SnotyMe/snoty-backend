plugins {
	kotlin("plugin.serialization")
	id("snoty.publish-conventions")
	id("snoty.publish-repo-conventions")
}

dependencies {
	api(projects.api)
}
