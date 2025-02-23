package me.snoty.integration.common.diff.state

import me.snoty.integration.common.diff.checksum
import org.bson.Document

data class EntityState(
	val id: String,
	val state: Document,
	val checksum: Long,
) {
	constructor(id: String, state: Document) : this(id, state, state.checksum())
}
