package me.snoty.backend.scheduling

import me.snoty.integration.common.wiring.Node

interface NodeScheduler {
	fun schedule(node: Node)
}
