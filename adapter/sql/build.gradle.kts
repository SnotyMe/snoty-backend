plugins {
	id("snoty.testintegration-conventions")
}

dependencies { with (libs) {
	compileOnly(projects.api)

	implementation(database.exposed.core)
	implementation(database.exposed.jdbc)
	implementation(database.exposed.json)
	implementation(database.exposed.datetime)

	implementation(database.postgres)
	implementation(database.hikari)

	implementation(configuration.hoplite.hikari) {
		exclude(group = "com.zaxxer", module = "HikariCP")
	}

	testImplementation(tests.testcontainers.postgres)
}}
