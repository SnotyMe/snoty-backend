@file:Suppress("UnstableApiUsage")

plugins {
	id("snoty.integration-conventions")
	id("snoty.testintegration-conventions")
}

dependencies { with(libs) {
	compileOnly(projects.api)
	compileOnly(projects.adapter.adapterApi)

	implementation(authentication.keycloak.adminClient)

	implementation(projects.adapter.oidc)

	testImplementation(libs.tests.testcontainers.keycloak) {
		// explicit dependency, the bundled version is buggy
		exclude(group = "org.keycloak")
	}
	testImplementation(libs.tests.json)
	testImplementation(libs.tests.ktor.server.testHost)
}}

testing.suites.withType<JvmTestSuite>().configureEach {
	dependencies {
		implementation(rootProject.sourceSets["dev"].runtimeClasspath)
		implementation(rootProject.sourceSets["test"].runtimeClasspath)
	}
}
