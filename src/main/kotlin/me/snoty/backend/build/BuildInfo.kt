package me.snoty.backend.build

import com.sksamuel.hoplite.ConfigAlias
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.snoty.backend.config.ApplicationConfigLoader
import me.snoty.backend.config.Config
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
