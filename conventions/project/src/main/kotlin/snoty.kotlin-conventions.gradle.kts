plugins {
	kotlin("jvm")
	id("org.jetbrains.kotlinx.kover")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xcontext-receivers", "-Xwhen-guards")
		optIn.addAll("kotlinx.coroutines.ExperimentalCoroutinesApi", "kotlin.uuid.ExperimentalUuidApi")
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
