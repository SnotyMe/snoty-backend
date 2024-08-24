import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
	kotlin("jvm")
	id("com.google.devtools.ksp")
}

fun VersionCatalog.getLibrary(name: String) =
	findLibrary(name).get().get()
fun VersionCatalog.getModule(name: String) =
	getLibrary(name).module

dependencies {
	val libs = project.rootProject.extensions
		.getByType<VersionCatalogsExtension>()
		.named("libs")

	val koinVersion = libs
		.findVersion("koin")
		.get()
		.displayName
	val koinAnnotationsVersion = libs
		.findVersion("koin-annotations")
		.get()
		.displayName

	implementation("${libs.getModule("koin-core")}:$koinVersion")
	api("${libs.getModule("koin-annotations")}:$koinAnnotationsVersion")

	ksp("${libs.getModule("koin-ksp")}:$koinAnnotationsVersion")
}

ksp {
	arg("KOIN_CONFIG_CHECK", "true")
}
