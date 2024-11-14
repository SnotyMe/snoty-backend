plugins {
	id("snoty.integration-conventions")
}

dependencies {
	// liquid for java
	implementation("nl.big-o:liqp:0.9.0.3")

	implementation(libs.libraries.ical4j)

	testImplementation(libs.tests.junit.api)
}
