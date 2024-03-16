package me.snoty.backend.build

import com.sksamuel.hoplite.ConfigAlias
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
