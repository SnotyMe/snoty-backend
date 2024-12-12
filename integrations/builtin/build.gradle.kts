plugins {
	id("snoty.integration-conventions")
}

dependencies {
	// liquid for java
	implementation("nl.big-o:liqp:0.9.1.3")
	implementation("io.github.java-diff-utils:java-diff-utils:4.15")

	implementation(libs.libraries.ical4j)

	testImplementation(libs.tests.junit.api)
}
