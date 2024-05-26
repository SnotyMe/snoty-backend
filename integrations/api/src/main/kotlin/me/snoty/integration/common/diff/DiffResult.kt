package me.snoty.integration.common.diff

import me.snoty.backend.database.mongo.ToBsonBuilder
import org.bson.Document

sealed class DiffResult : ToBsonBuilder {
	data object Unchanged : DiffResult()
	data class Created(val checksum: Long, val fields: Fields) : DiffResult()
	data class Updated(val checksum: Long, val diff: Diff) : DiffResult()
	data class Deleted(val checksum: Long, val previous: Fields) : DiffResult()

	override fun buildDocument(document: Document) {
		when (this) {
			is Created -> {
				document["checksum"] = checksum
				document["fields"] = fields
			}
			is Updated -> {
				document["checksum"] = checksum
				document["diff"] = diff
			}
			is Deleted -> {
				document["checksum"] = checksum
				document["previous"] = previous
			}
			is Unchanged -> {}
		}
		document["type"] = this::class.simpleName!!
	}
}
