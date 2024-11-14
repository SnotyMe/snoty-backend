package me.snoty.integration.notion

import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.annotation.Single

data class NotionConfig(
	val clientId: String,
	val clientSecret: String,
)

@Single
fun provideNotionHandlerSettings(configLoader: ConfigLoader) = configLoader.load<NotionConfig>("notion")
