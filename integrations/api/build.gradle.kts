plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.kover)
}

dependencies {
	api(projects.api)

	implementation(database.mongodb)

	api(ktor.client.core)
	api(ktor.client.apache)
	api(ktor.client.contentNegotiation)
	api(ktor.serialization.kotlinx.json)
	api(ktor.server.core)
	api(ktor.server.auth)

	api(monitoring.micrometer)
	api(monitoring.ktor.opentelemetry)

	api(libraries.jackson.core)
	api(libraries.jackson.kotlin)
	api(libraries.bson.kotlinx)

	// liquid for java
	implementation("nl.big-o:liqp:0.9.0.3")

	testImplementation(kotlin("test"))
	testImplementation(tests.mockk)
}

base.archivesName = "integration-api"

subprojects {
	base.archivesName = "integration-${this.name}"
}

tasks.test {
	useJUnitPlatform()
}
