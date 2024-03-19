package me.snoty.backend.build

import com.sksamuel.hoplite.ConfigAlias
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
