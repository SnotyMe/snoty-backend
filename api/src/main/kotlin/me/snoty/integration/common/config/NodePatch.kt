package me.snoty.integration.common.config

import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import org.slf4j.event.Level
import java.util.*

data class NodePatch(
	val name: String? = null,
	val position: NodePosition? = null,
	val logLevel: Optional<Level?>? = null,
	val settings: NodeSettings? = null,
)
