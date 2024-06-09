package me.snoty.backend.test

import io.mockk.mockk
import kotlinx.datetime.Clock
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.*

val TestConfig = Config(
	port = 8080,
	environment = Environment.TEST,
	publicHost = "http://localhost:8080",
	mongodb = mockk(),
	authentication = OidcConfig(
		serverUrl = "http://localhost:8081",
		clientId = "",
		clientSecret = ""
	)
)

class TestConfigBuilder(block: TestConfigBuilder.() -> Unit) {
	var port: Short = 8080
	var environment: Environment = Environment.TEST
	var publicHost: String = "http://localhost:8080"
	var mongodb: MongoConfig = mockk()
	var authentication: OidcConfig = OidcConfig(
		serverUrl = "http://localhost:8081",
		clientId = "",
		clientSecret = ""
	)

	init {
		block()
	}

	fun build() = Config(
		port = port,
		environment = environment,
		publicHost = publicHost,
		mongodb = mongodb,
		authentication = authentication
	)
}

fun buildTestConfig(block: TestConfigBuilder.() -> Unit)
	= TestConfigBuilder(block).build()

val TestBuildInfo = BuildInfo(
	gitBranch = "<test>",
	gitCommit = "<test>",
	gitCommitDate = Clock.System.now(),
	buildDate = Clock.System.now(),
	version = "<test>",
	application = "snoty-backend"
)
