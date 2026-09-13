package me.snoty.integration.common.wiring.node.template

import me.snoty.core.node.NodeType

interface NodeTemplateRegistry {
	fun getAllTemplates(): Map<NodeType, List<NodeTemplate>>
}
