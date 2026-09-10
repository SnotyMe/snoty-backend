plugins {
	kotlin("jvm")
	id("org.jetbrains.kotlinx.kover")
}

kotlin {
	compilerOptions {
		optIn.addAll(
			"kotlinx.coroutines.ExperimentalCoroutinesApi",
			"kotlin.uuid.ExperimentalUuidApi",
			"kotlin.time.ExperimentalTime",
			"io.ktor.utils.io.ExperimentalKtorApi",
		)
	}
}

kover {
	merge {
		allProjects()
	}
	currentProject {
		sources {
			includedSourceSets.add(sourceSets.main.name)
		}
	}
}
