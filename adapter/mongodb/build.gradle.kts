plugins {
	id("snoty.testintegration-conventions")
	id("snoty.publish-conventions")
}

dependencies { with(libs) {
	compileOnly(projects.api)
	implementation(monitoring.opentelemetry.semconv)

	api(database.mongodb)
	api(database.mongodb.sync)

	testIntegrationImplementation(tests.testcontainers.mongodb)
}}
