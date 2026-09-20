package me.snoty.node.discord

import kotlinx.serialization.Serializable
import me.snoty.backend.schema.Multiline

object DiscordWebhook {
	@Serializable
	data class Message(
		@Multiline
		val content: String?,
		val username: String? = null,
		val avatarUrl: String? = null,
		val embeds: List<Embed> = emptyList()
	)

	@Serializable
	data class Embed(
		val title: String? = null,
		val description: String? = null,
		val url: String? = null,
		val color: Int? = null,
		val fields: List<Field> = emptyList()
	)

	@Serializable
	data class Field(
		val name: String,
		val value: String,
		val inline: Boolean = false
	)
}
