package me.snoty.integration.notion.page

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.snoty.integration.notion.model.NotionParent
import me.snoty.integration.notion.page.MappedNotionPageProperties.*

@Serializable
data class NotionPage(
	val id: String,

	val properties: Map<String, NotionPageProperties>,
)

fun NotionPage.toPageCreateDTO(parent: NotionParent) = NotionPageCreateDTO(
	parent = parent.copy(
		pageId = parent.pageId?.ifBlank { null },
		databaseId = parent.databaseId?.ifBlank { null },
	),
	properties = properties.mapValues { (_, value) -> value.map() },
)

fun NotionPage.toPageUpdateDTO() = NotionPageUpdateDTO(
	properties = properties.mapValues { (_, value) -> value.map() },
)

@Serializable
data class NotionPageCreateDTO(
	val parent: NotionParent,
	val properties: Map<String, MappedNotionPageProperties>,
)

@Serializable
data class NotionPageCreateResponse(
	val id: String,
)

@Serializable
data class NotionPageUpdateDTO(
	val properties: Map<String, MappedNotionPageProperties>,
)

@Serializable
sealed class NotionPageProperties {
	abstract fun map(): MappedNotionPageProperties

	@Serializable
	@SerialName("Title")
	data class Title(private val text: String) : NotionPageProperties() {
		override fun map() = Title(title = listOf(Text(text = Content(content = text))))
	}

	@Serializable
	@SerialName("RichText")
	data class RichText(private val text: String): NotionPageProperties() {
		override fun map() = RichText(richText = listOf(Text(text = Content(content = text))))
	}

	@Serializable
	@SerialName("Number")
	data class Number(val number: Double): NotionPageProperties() {
		override fun map() = MappedNotionPageProperties.Number(number = number)
	}

	@Serializable
	@SerialName("Date")
	data class Date(val date: Instant): NotionPageProperties() {
		override fun map() = Date(date = DateObj(start = date))
	}
}

@Serializable
sealed class MappedNotionPageProperties {
	@Serializable
	@SerialName("title")
	data class Title(val title: List<Text>) : MappedNotionPageProperties()
	@Serializable
	@SerialName("rich_text")
	data class RichText(@SerialName("rich_text") val richText: List<Text>) : MappedNotionPageProperties()

	@Serializable
	data class Text(val text: Content)
	@Serializable
	data class Content(val content: String)

	@Serializable
	@SerialName("number")
	data class Number(val number: Double) : MappedNotionPageProperties()

	@Serializable
	@SerialName("date")
	data class Date(val date: DateObj) : MappedNotionPageProperties()

	@Serializable
	data class DateObj(val start: Instant)
}
