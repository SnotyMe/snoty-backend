plugins {
	kotlin("jvm")
	`jvm-test-suite`
}

lateinit var testIntegration: NamedDomainObjectProvider<JvmTestSuite>

testing {
	suites {
		val test by getting(JvmTestSuite::class)
		testIntegration = register<JvmTestSuite>("testIntegration") {
			dependencies {
				implementation(project())
				implementation(sourceSets.test.get().output)
			}
			sources.compileClasspath += sourceSets.test.get().compileClasspath
			sources.runtimeClasspath += sourceSets.test.get().runtimeClasspath
			targets {
				all {
					testTask.configure {
						shouldRunAfter(test)
					}
				}
			}
		}

		withType<JvmTestSuite> {
			useJUnitJupiter()

			dependencies {
				implementation(testFixtures(project(":api")))
			}

			tasks.withType<Test>().configureEach {
				maxParallelForks = (Runtime.getRuntime().availableProcessors()).coerceAtLeast(1)
				environment("LOG_LEVEL", "TRACE")
			}
		}
	}
}

tasks.check {
	dependsOn(testIntegration)
}
