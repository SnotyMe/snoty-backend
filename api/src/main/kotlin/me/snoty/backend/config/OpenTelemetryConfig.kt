package me.snoty.backend.config

data class OpenTelemetryConfig(
	val resourcePaths: List<String> = emptyList(),
)
