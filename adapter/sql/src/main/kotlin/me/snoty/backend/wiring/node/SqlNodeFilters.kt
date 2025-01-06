package me.snoty.backend.wiring.node

import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

fun SqlExpressionBuilder.positionFilter(nodeTable: NodeTable, nodeRegistry: NodeRegistry, position: NodePosition): Op<Boolean> {
	val nodes = nodeRegistry.lookupDescriptorsByPosition(position)
	return nodes.fold<NodeDescriptor, Op<Boolean>>(Op.FALSE) { acc, node ->
		// chain together OR conditions for each node descriptor that matches the position
		acc or ((nodeTable.descriptor_namespace eq node.namespace) and (nodeTable.descriptor_name eq node.name))
	}
}
