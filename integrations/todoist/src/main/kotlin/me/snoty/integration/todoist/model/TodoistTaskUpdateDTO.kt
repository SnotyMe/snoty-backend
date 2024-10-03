package me.snoty.integration.todoist.model

import kotlinx.serialization.Serializable

@Serializable
data class TodoistTaskUpdateDTO(
	val content: String?,
	val description: String?,
	val labels: List<String>?,
	val priority: Int?,
	val dueDate: String?,
	val dueDateTime: String?,
)
