package me.snoty.backend.scheduling

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KFunction

interface AdminTasks {
	suspend fun scheduleMissingJobs()
	suspend fun renameExistingJobs()

	fun getTasks() = listOf(
		Task("Schedule missing jobs", ::scheduleMissingJobs),
		Task("Rename existing jobs", ::renameExistingJobs),
	)
}

@Suppress("UNCHECKED_CAST")
@Serializable
data class Task(val displayName: String, val name: String, @Transient val action: suspend () -> Unit = {}) {
	constructor(displayName: String, functionRef: KFunction<*>) :
		this(displayName, functionRef.name, functionRef as suspend () -> Unit)
}
