package me.snoty.integration.todoist

import kotlinx.serialization.Serializable
import me.snoty.backend.utils.orNull
import me.snoty.integration.todoist.model.TodoistTaskCreateDTO
import me.snoty.integration.todoist.model.TodoistTaskUpdateDTO

@Serializable
data class TodoistInput(
	val id: String,

	val parentId: String?,
	val content: String,
	val description: String?,
	val labels: List<String>?,
	val priority: Int?,
	val dueDate: String?,
	val dueDateTime: String?,
)

fun TodoistInput.toTaskCreateDTO(projectId: String?, sectionId: String?) = TodoistTaskCreateDTO(
	content = content,
	description = description,
	labels = labels,
	priority = priority,
	dueDate = dueDate,
	dueDateTime = dueDateTime,
	projectId = projectId.orNull(),
	sectionId = sectionId.orNull(),
	parentId = parentId,
)

fun TodoistInput.toTaskUpdateDTO() = TodoistTaskUpdateDTO(
	content = content,
	description = description,
	labels = labels ?: emptyList(),
	priority = priority,
	dueDate = dueDate,
	dueDateTime = dueDateTime,
)
