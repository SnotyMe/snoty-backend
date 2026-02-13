import kotlin.jvm.optionals.getOrNull

plugins {
	kotlin("jvm")
	id("io.insert-koin.compiler.plugin")
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

	implementation("${libs.getModule("koin-core")}:$koinVersion")
	implementation("${libs.getModule("koin-ktor")}:$koinVersion")
	api("${libs.getModule("koin-annotations")}:$koinVersion")
}

koinCompiler {
	userLogs = System.getenv("KOIN_USER_LOGS") == "true"
	debugLogs = System.getenv("KOIN_DEBUG_LOGS") == "true"
}
