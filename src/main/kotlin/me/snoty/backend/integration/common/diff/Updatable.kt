package me.snoty.backend.integration.common.diff

import kotlinx.serialization.json.JsonObject

data class OldNew<T>(val old: T, val new: T)
typealias Diff = Map<String, OldNew<Any>>

typealias Fields = JsonObject
fun Fields.checksum() = hashCode().toLong()

sealed class DiffResult {
	data object NoChange : DiffResult()
	data class Created(val checksum: Long, val fields: Fields) : DiffResult()
	data class Updated(val checksum: Long, val diff: Diff) : DiffResult()
	data class Deleted(val checksum: Long, val previous: Fields) : DiffResult()
}
