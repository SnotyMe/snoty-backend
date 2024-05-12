package me.snoty.integration.common.diff

import kotlinx.serialization.json.*

data class OldNew<T>(val old: T, val new: T)
typealias Diff = Map<String, OldNew<Any>>

typealias Fields = JsonObject
fun Fields.checksum() = hashCode().toLong()

fun Fields.getString(key: String): String = get(key)!!.jsonPrimitive.content
fun Fields.getInt(key: String): Int = get(key)!!.jsonPrimitive.int
fun Fields.getLong(key: String): Long = get(key)!!.jsonPrimitive.long
fun Fields.getJsonArray(key: String): JsonArray = get(key)!!.jsonArray

sealed class DiffResult {
	data object Unchanged : DiffResult()
	data class Created(val checksum: Long, val fields: Fields) : DiffResult()
	data class Updated(val checksum: Long, val diff: Diff) : DiffResult()
	data class Deleted(val checksum: Long, val previous: Fields) : DiffResult()
}
