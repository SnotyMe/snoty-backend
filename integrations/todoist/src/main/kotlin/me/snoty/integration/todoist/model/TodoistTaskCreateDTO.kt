package me.snoty.integration.todoist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
/**
 * [Todoist REST Docs](https://developer.todoist.com/rest/v2/#create-a-new-task)
 */
data class TodoistTaskCreateDTO(
	val content: String,
	val description: String?,
	@SerialName("project_id")
	val projectId: String?,
	@SerialName("section_id")
	val sectionId: String?,
	@SerialName("parent_id")
	val parentId: String?,
	@SerialName("labels")
	val labels: List<String>?,
	@SerialName("priority")
	val priority: Int?,
	@SerialName("due_date")
	val dueDate: String?,
	@SerialName("due_datetime")
	val dueDateTime: String?,
)

@Serializable
data class TodoistTaskCreateResponse(
	val id: String
)
