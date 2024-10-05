package me.snoty.integration.todoist

import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.annotation.Single

data class TodoistConfig(
	val clientId: String,
	val clientSecret: String,
)

@Single
fun provideTodoistHandlerSettings(configLoader: ConfigLoader) = configLoader.load<TodoistConfig>("todoist")
