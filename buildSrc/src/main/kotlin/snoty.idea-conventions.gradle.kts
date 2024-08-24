import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
	id("org.jetbrains.gradle.plugin.idea-ext")
}

afterEvaluate {
	idea {
		module {
			// long import times but worth it as, without it, functions may not have proper documentation
			isDownloadJavadoc = true
			isDownloadSources = true
		}

		project {
			settings {
				runConfigurations {
					create("Application [dev]", Application::class.java).apply {
						mainClass = extensions.getByType(JavaApplication::class.java).mainClass.get()
						moduleName = "snoty-backend.dev"
						jvmArgs = "-Dio.ktor.development=true"

						envs = mutableMapOf(
							"LOG_LEVEL" to "TRACE",
						)
					}
				}
			}
		}
	}
}
