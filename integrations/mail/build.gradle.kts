plugins {
	id("snoty.integration-conventions")
}

dependencies {
	implementation("jakarta.mail:jakarta.mail-api:2.1.3")
	runtimeOnly("org.eclipse.angus:angus-mail:2.0.3")
}
