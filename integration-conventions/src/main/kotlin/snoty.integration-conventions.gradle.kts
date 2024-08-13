plugins {
	kotlin("jvm")
	id("com.google.devtools.ksp")
}

apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
apply(plugin = "org.jetbrains.kotlinx.kover")

dependencies {
	implementation(project(":api"))
	implementation(project(":integrations:api"))

	ksp(project(":integration-plugin"))
}

sourceSets.test.configure {
	val integrationsApi = project(":integrations:api").sourceSets.test.get().output
	compileClasspath += integrationsApi
	runtimeClasspath += integrationsApi
}

tasks.test {
	useJUnitPlatform()
}
