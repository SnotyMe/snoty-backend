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
		// awaiting implementation of https://github.com/Kotlin/kotlinx-kover/issues/714
		afterEvaluate {
			sources {
				// per default, kover only excludes `test`
				// since we also have `testIntegration` and `dev`, we have to exclude them
				sourceSets
					.filter { it.name != sourceSets.main.name }
					.forEach {
						excludedSourceSets.add(it.name)
					}
			}
		}
	}
}
