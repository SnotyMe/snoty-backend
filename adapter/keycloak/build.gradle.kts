plugins {
	id("snoty.integration-conventions")
	id("snoty.testintegration-conventions")
}

dependencies { with(libs) {
	compileOnly(projects.api)
	compileOnly(projects.adapter.adapterApi)

	implementation(authentication.keycloak.adminClient)

	implementation(projects.adapter.oidc)
}}

testing.suites.withType<JvmTestSuite>().configureEach {
	dependencies {
		implementation(libs.tests.testcontainers.keycloak) {
			// explicit dependency, the bundled version is buggy
			exclude(group = "org.keycloak")
		}
		implementation(libs.tests.json)
		implementation(libs.tests.ktor.server.testHost)
		implementation(projects.snotyBackend.dependencyProject.sourceSets["dev"].runtimeClasspath)
		implementation(projects.snotyBackend.dependencyProject.sourceSets["test"].runtimeClasspath)
	}
}
