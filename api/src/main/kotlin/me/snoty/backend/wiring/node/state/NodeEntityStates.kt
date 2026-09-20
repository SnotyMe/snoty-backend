package me.snoty.backend.wiring.node.state

import org.bson.Document

data class EntityState(
	val id: String,
	val state: Document,
	val checksum: Long,
) {
	constructor(id: String, state: Document) : this(id, state, state.checksum())
}
