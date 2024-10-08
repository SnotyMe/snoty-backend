package me.snoty.integration.common.diff

import me.snoty.integration.common.diff.state.EntityState
import org.bson.Document

fun Document?.diff(lastState: EntityState?): DiffResult = when {
	this == null && lastState == null -> DiffResult.Unchanged
	this == null && lastState != null -> DiffResult.Deleted(lastState.checksum, lastState.state)
	// will never happen, as the first conditions catch all those cases
	this == null -> error("Cannot diff a null document")
	lastState == null -> DiffResult.Created(checksum(), this)
	checksum() == lastState.checksum -> DiffResult.Unchanged
	else -> diff(lastState.state)
}

fun Document.diff(old: Document): DiffResult {
	val createdOrChanged = this.entries
		.filter { (key, value) -> value != old[key] }
		.associate { (key, value) ->
			key to Change(old = old[key], new = value)
		}

	// keys that previously existed
	val deleted = old.entries
		.filter { (key, _) -> !containsKey(key) }
		.associate { (key, value) ->
			key to Change(old = value, new = null)
		}

	val diff: Diff = createdOrChanged + deleted

	return when {
		diff.isEmpty() -> DiffResult.Unchanged
		else -> DiffResult.Updated(checksum(), diff)
	}
}

typealias Diff = Map<String, Change<*, *>>

fun Document.checksum() = hashCode().toLong()
