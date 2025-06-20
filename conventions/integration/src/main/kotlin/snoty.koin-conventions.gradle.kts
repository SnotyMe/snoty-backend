import kotlin.jvm.optionals.getOrNull

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
		.run {
			runCatching {
				named("snoty")
			}.getOrElse {
				named("libs")
			}
		}

	val koinVersion = libs
		.findVersion("koin")
		.getOrNull()
		?.displayName
			?: throw IllegalStateException("No koin version")
	val koinAnnotationsVersion = libs
		.findVersion("koin-annotations")
		.getOrNull()
		?.displayName
			?: throw IllegalStateException("No koin-annotations version")

	constraints {
		implementation("${libs.getModule("koin-core")}:$koinVersion") {
			version {
				strictly(koinVersion) // TODO: remove once Koin 4.1 is fixed (https://github.com/InsertKoinIO/koin/pull/2231)
			}
		}
	}
	implementation("${libs.getModule("koin-ktor")}:$koinVersion")
	api("${libs.getModule("koin-annotations")}:$koinAnnotationsVersion")

	ksp("${libs.getModule("koin-ksp")}:$koinAnnotationsVersion")
}
