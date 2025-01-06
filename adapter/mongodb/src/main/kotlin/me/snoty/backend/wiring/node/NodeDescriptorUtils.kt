package me.snoty.backend.wiring.node

import com.mongodb.client.model.Filters
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.conversions.Bson

fun NodeDescriptor.Companion.filter(namespace: String, name: String): Bson = Filters.and(
	Filters.eq(NodeDescriptor::namespace.name, namespace),
	Filters.eq(NodeDescriptor::name.name, name)
)
