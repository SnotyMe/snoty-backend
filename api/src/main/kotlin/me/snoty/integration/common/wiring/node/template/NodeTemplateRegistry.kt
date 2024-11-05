package me.snoty.integration.common.wiring.node.template

import me.snoty.integration.common.wiring.node.NodeDescriptor

interface NodeTemplateRegistry {
	fun getAllTemplates(): Map<NodeDescriptor, List<NodeTemplate>>
}
