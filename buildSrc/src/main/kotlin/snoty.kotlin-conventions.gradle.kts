plugins {
	kotlin("jvm")
	id("org.jetbrains.kotlinx.kover")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xcontext-receivers")
		optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
	}
}

kover {
	merge {
		allProjects()
	}
	currentProject {
		sources {
			// per default, kover only excludes `test`
			// since we also have `testIntegration`, we have to exclude it
			testing.suites
				.filterIsInstance<JvmTestSuite>()
				.forEach {
					excludedSourceSets.add(it.sources.name)
				}
		}
	}
}
