import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
	id("org.jetbrains.gradle.plugin.idea-ext")
}

idea {
	module {
		// long import times but worth it as, without it, functions may not have proper documentation
		isDownloadJavadoc = true
		isDownloadSources = true
	}

	project {
		settings {
			runConfigurations {
				// this run configuration emulates the `run` task, but without gradle
				// this *should* give better hot swap and performance
				create("Application [dev]", Application::class.java).apply {
					mainClass = "me.snoty.backend.ApplicationKt"
					moduleName = "snoty-backend.dev"
					jvmArgs = "-Dio.ktor.development=true"

					envs = mutableMapOf(
						"LOG_LEVEL" to "TRACE",
						"SERVER_LOG_LEVEL" to "INFO"
					)
				}
			}
		}
	}
}
