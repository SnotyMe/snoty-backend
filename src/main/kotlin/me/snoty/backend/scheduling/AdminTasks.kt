package me.snoty.backend.scheduling

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KFunction

interface AdminTasks {
	fun getTasks(): List<Task>
}

@Suppress("UNCHECKED_CAST")
@Serializable
data class Task(val displayName: String, val name: String, @Transient val action: suspend () -> Unit = {}) {
	constructor(displayName: String, functionRef: KFunction<*>) :
		this(
			displayName = displayName,
			name = functionRef.name,
			action = functionRef as? suspend () -> Unit ?: { (functionRef as () -> Unit)() },
		)
}
