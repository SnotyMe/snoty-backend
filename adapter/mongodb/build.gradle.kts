plugins {
	alias(libs.plugins.kotlin.serialization)
	id("snoty.testintegration-conventions")
	id("snoty.publish-conventions")
}

dependencies { with(libs) {
	compileOnly(projects.api)
	implementation(monitoring.opentelemetry.semconv)
	implementation("com.github.zafarkhaja:java-semver:0.10.2")

	api(database.mongodb)
	api(database.mongodb.sync)

	testIntegrationImplementation(tests.testcontainers.mongodb)
}}
