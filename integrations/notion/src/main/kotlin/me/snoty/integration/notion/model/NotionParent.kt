package me.snoty.integration.notion.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NotionParent(
	@SerialName("database_id")
	@EncodeDefault(Mode.NEVER)
	val databaseId: String? = null,
	@SerialName("page_id")
	@EncodeDefault(Mode.NEVER)
	val pageId: String? = null,
)
