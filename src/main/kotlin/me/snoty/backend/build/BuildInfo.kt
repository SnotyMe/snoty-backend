package me.snoty.backend.build

import com.sksamuel.hoplite.ConfigAlias
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.snoty.backend.config.Config
import me.snoty.backend.config.ConfigLoader
import org.koin.core.annotation.Single

@Serializable
data class BuildInfo(
	@ConfigAlias("Git-Branch")
	val gitBranch: String,
	@ConfigAlias("Git-Commit")
	val gitCommit: String,
	@ConfigAlias("Git-Committer-Date")
	val gitCommitDate: Instant,
	@ConfigAlias("Build-Date")
	val buildDate: Instant,
	@ConfigAlias("Version")
	val version: String,
	@ConfigAlias("Application")
	val application: String
)

val DevBuildInfo = BuildInfo(
	gitBranch = "dev",
	gitCommit = "dev",
	gitCommitDate = Clock.System.now(),
	buildDate = Clock.System.now(),
	version = "dev",
	application = "snoty-backend"
)

@Single
fun provideBuildInfo(config: Config, configLoader: ConfigLoader): BuildInfo = try {
		configLoader.loadBuildInfo()
	} catch (e: Exception) {
		// when ran without gradle, the build info is not available
		// it'll just default to dev build info in this case
		if (config.environment.isDev()) {
			KotlinLogging.logger {}.warn { "Failed to load build info: ${e.message}" }
			DevBuildInfo
		} else {
			throw e
		}
	}
