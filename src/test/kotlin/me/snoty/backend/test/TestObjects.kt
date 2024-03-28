package me.snoty.backend.test

import com.zaxxer.hikari.HikariDataSource
import io.mockk.mockk
import kotlinx.datetime.Clock
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.Config
import me.snoty.backend.config.DatabaseConfig
import me.snoty.backend.config.Environment

val TestConfig = Config(
	port = 8080,
	environment = Environment.TEST,
	publicHost = "http://localhost:8080",
	database = DatabaseConfig(mockk<HikariDataSource>()),
	authentication = mockk()
)

val TestBuildInfo = BuildInfo(
	gitBranch = "<test>",
	gitCommit = "<test>",
	gitCommitDate = Clock.System.now(),
	buildDate = Clock.System.now(),
	version = "<test>",
	application = "snoty-backend"
)
