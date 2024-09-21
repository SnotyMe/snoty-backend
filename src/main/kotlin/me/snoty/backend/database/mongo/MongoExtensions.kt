package me.snoty.backend.database.mongo

import me.snoty.integration.common.wiring.node.NodeDescriptor

val NodeDescriptor.mongoCollectionPrefix: String
	get() = "$subsystem.$type"
