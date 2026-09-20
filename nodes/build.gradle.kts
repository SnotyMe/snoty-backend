plugins {
	id("snoty.integration-conventions")
}

dependencies {
	// mail nodes
	implementation("jakarta.mail:jakarta.mail-api:2.1.3")
	runtimeOnly("org.eclipse.angus:angus-mail:2.0.3")

	// liquid for java
	implementation("nl.big-o:liqp:0.9.1.3")
	implementation("io.github.java-diff-utils:java-diff-utils:4.15")

	implementation(libs.libraries.ical4j)

	testImplementation(testFixtures(projects.api))
}
