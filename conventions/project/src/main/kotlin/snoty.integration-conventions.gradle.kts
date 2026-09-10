plugins {
	kotlin("jvm")
	id("com.google.devtools.ksp")
}

apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
apply(plugin = "org.jetbrains.kotlinx.kover")

dependencies {
	implementation(project(":api"))

	ksp(project(":integration-plugin"))
}

sourceSets.test.configure {
	val api = project(":api").sourceSets.test.get().output
	compileClasspath += api
	runtimeClasspath += api
}

tasks.test {
	useJUnitPlatform()
}
