package me.snoty.backend.wiring.node.template

import me.snoty.core.node.NodeType

interface NodeTemplateRegistry {
	fun getAllTemplates(): Map<NodeType, List<NodeTemplate>>
}
