package me.snoty.integration.common.wiring.flow

import kotlinx.serialization.Serializable
import me.snoty.core.NodeId
import org.slf4j.event.Level
import kotlin.time.Instant

@Serializable
data class NodeLogEntry(
	val timestamp: Instant,
	val level: Level,
	val message: String,
	val node: NodeId?,
)

@Serializable
data class NodeLogEntryDto(
	val id: String,
	val timestamp: Instant,
	val level: Level,
	val message: String,
	val node: NodeId?,
)
