plugins {
	id("snoty.testintegration-conventions")
}

dependencies { with(libs) {
	compileOnly(projects.api)
	implementation(monitoring.opentelemetry.semconv)

	implementation(database.mongodb)
	implementation(database.mongodb.sync)

	testIntegrationImplementation(tests.testcontainers.mongodb)
}}
