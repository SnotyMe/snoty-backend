package me.snoty.backend.build

import me.snoty.backend.config.ApplicationConfigLoader
import me.snoty.backend.config.Config
import org.koin.core.annotation.Single
import kotlin.time.Clock

val DevBuildInfo = BuildInfo(
	gitBranch = "dev",
	gitCommit = "dev",
	gitCommitDate = Clock.System.now(),
	buildDate = Clock.System.now(),
	version = "dev",
	application = "snoty-backend"
)

@Single
fun provideBuildInfo(config: Config, configLoader: ApplicationConfigLoader): BuildInfo = try {
		configLoader.loadBuildInfo()
	} catch (e: Exception) {
		when {
			// when ran without gradle, the build info is not available
			// it'll just default to dev build info in this case
			config.environment.isDev() -> DevBuildInfo
			else -> throw e
		}
	}
